"use strict";

const express = require('express')
const http = require('http');
const path = require('path');
const fs = require('fs');


let port = 5544;
process.env.UI_PORT = port


let app = express();

let server = http.createServer(app);
let io = require('socket.io')(server);

io.on('connection', function (socket) {
	// do socket stuff
	socket.on('relay',obj => {
		console.log("received",obj)
	});
});



app.get('/', function (req, res) {
	res.sendFile(path.resolve('public/index.html'))
})

app.use(express.static('public'))


server.listen(port, function () {
	console.log('Example app listening on port %d!', port)

})

