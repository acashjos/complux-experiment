"use strict";


var WebSocketServer = require('websocket').server;
var http = require('http');
const UI = require('./UIserver');

let server = null;
var httpServer = null;
let timeout = 0;
let connected = false;

exports.start = (port) => {
	if (server) return server;
	console.log("\n\nstarting socket server...")
	startCountdown();

	httpServer = http.createServer(function (request, response) {
		console.log((new Date()) + ' Received request for ' + request.url);
		response.writeHead(404);
		response.end();
	});
	httpServer.listen(port, function () {
		console.log((new Date()) + ' Server is listening on port ' + port);
	});

	server = new WebSocketServer({
		httpServer: httpServer,
		autoAcceptConnections: false
	});

	function originIsAllowed(origin) {
		// allow connection from 1st client only.
		return !connected;
	}

	server.on('request', function (request) {
		if (!originIsAllowed(request.origin)) {
			// Make sure we only accept requests from an allowed origin 
			request.reject();
			console.log((new Date()) + ' Connection from origin ' + request.origin + ' rejected.');
			return;
		}

		clearTimeout(timeout);
		connected = true;

		var connection = request.accept(/*'plux-protocol'*/ null , request.origin);

		console.log((new Date()) + ' Connection accepted.');

		let expecting = null
		let expectationQueue = []
		function emit(appId,action,data,cb){
			if(expecting){
				expectationQueue.push(arguments);
				return;
			}
			connection.sendUTF(JSON.stringify({appId, action,data}));
			console.log('Sending...',{appId, action,data})
			expecting = cb
		}

		function popEmitQueue(){

			expecting = null;
			if(!expectationQueue.length) return;
			let nextargs = expectationQueue.splice(0,1)[0]
			emit.apply(null,nextargs);
		}

		expecting = (deviceInfo) => UI.start(emit, deviceInfo)

		connection.on('message', function (message) {
			// let cb = expecting;
			// expecting = null;
			if (message.type === 'utf8') {
				console.log('Received Message: ' + message.utf8Data);
				expecting ? expecting(message.utf8Data) : UI.update(message.utf8Data)

			}
			else if (message.type === 'binary') {
				if(!expecting) return;
				console.log('Received Binary Message of ' + message.binaryData.length + ' bytes');
				expecting(message.binaryData);
			}

			popEmitQueue()
		});
		connection.on('close', function (reasonCode, description) {
			console.log((new Date()) + ' Peer ' + connection.remoteAddress + ' disconnected.');
			startCountdown();

		});
	});
}

exports.stop = stop

function startCountdown(){
	connected = false;
	UI.stop();
	require('./index').cleanFiles();
	// wait 3 minutes and stop if no client connects.
	timeout = setTimeout(stop,3 * 60 * 1000); //3 minutes
}


function stop() {
	if(server) server.shutDown();
	if(httpServer) httpServer.close();
	httpServer = null;
	server = null;
	console.log("\n STOP CALLED.. EXITING!!\n\n\n\n\n")
}

