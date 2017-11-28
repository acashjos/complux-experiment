"use strict";

const express = require('express')
const http = require('http');
const contextHolder = require('./deviceContext')
const path = require('path');
const fs = require('fs');

let app;
let io;

exports.start = function start(emit, deviceInfo) {

	let port = 5544;
	process.env.UI_PORT = port
	if (app) return;

	contextHolder.setDevice(JSON.parse(deviceInfo));
	app = express();

	var server = http.createServer(app);
	io = require('socket.io')(server);
	io.on('connection', function (socket) {
		let appId = socket.request._query.app
		console.log(appId);
		socket.join(appId);
		socket.on('relay', (pkg) => {
			// pkg.appId = appId;
			emit(appId,"relay", pkg);
		})

		emit(appId,"launch",null)
	});
	

	app.use(express.static('complux-ui-dev/public'))

	app.get('/drawable/wallpaper', function (req, res) {
		console.log(contextHolder.getContext());
		res.writeHead(200, {
			'Content-Type': 'image/png',
			'Content-Length': contextHolder.getContext().wallpaper.length
		});
		res.end(contextHolder.getContext().wallpaper)
	})


	var sandbox = express.Router();

	sandbox.get('/', function (req, res) {
		res.sendFile(path.resolve('complux-ui-dev/public/index.html'))
	})

	sandbox.get('/drawable/icon/', function (req, res) {
		let img = contextHolder.getContext().appList[req.appId].icon
		res.writeHead(200, {
			'Content-Type': 'image/png',
			'Content-Length': img.length
		});
		res.end(img)
	})

	sandbox.get('/assets/:filename', function (req, res) {
		let tmppath = path.resolve('tmp/' + req.appId + '-' + req.params.filename)
		if(process.env.mock=='ui') tmppath = path.resolve('complux-ui-dev/public/assets/'+req.params.filename)
		console.log("accessing file",tmppath)
		if (fs.existsSync(tmppath)) {
			return res.sendFile(tmppath);
		}
		emit(req.appId,"getFile", { filename: req.params.filename }, (buff) => {
			res.writeHead(200, {
				'Content-Length': buff.length
			});
			res.end(buff)
			fs.writeFile(tmppath, buff, () => {
				require('./index').rememberToClean(tmppath);
			})
		})

	})


	app.use('/:appId', (req, res, next) => {
		req.appId = req.params.appId;
		next()
	})
	app.use('/:appId', sandbox)

	server.listen(port, function () {
		console.log('Example app listening on port %d!', port)

		contextHolder.init(emit)
		/* (lastApp) => {
			let runnableName = lastApp.match(/.*\/([^\/]+).desktop$/)[1]
			console.log("gtk-launch " + runnableName)
			const spawn = require('child_process').spawn;
			const browser = spawn('gtk-launch', [runnableName]);
			browser.on('close', (code) => {
				console.log(`child process exited with code ${code}`);
			});
		}*/

	})

}

exports.update = function (string) {
	// do updation
	const update = JSON.parse(string);
	// contextHolder.updateState(update);
	io.to(update.appId).emit('stateChange',update);

}

exports.stop = function () {
	if (!app) return;
	// send a window.close instruction through socket.io. stop the server in a settimeout.
	setTimeout(app.close.bind(null), 2000);
	app = null;
}