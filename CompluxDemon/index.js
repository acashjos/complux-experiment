'use strict';

process.stdin.resume();//so the program will not close instantly

var os = require('os');
var path = require('path');
var fs = require('fs');
var dgram = require("dgram");

const CLIENT_NAME = process.argv[2] || "@" + Date.now();
const CLIENT_PORT = (process.argv[3] || 17171)


// DIRTY
var logtemp = console.log
console.log = function(){
  // logtemp(arguments)
  logtemp.apply(console,["-------------------------------------------------\n",...arguments])
}

if (CLIENT_NAME.length > 50) throw new Error("Too long for a name!! try something shorter")

var client = dgram.createSocket("udp4");

client.on("message", function (msg, rinfo) {
  console.log("client got: " + msg + " from " +
    rinfo.address + ":" + rinfo.port);
  let myAddress = getIPOnSourceIFace(rinfo.address)

  let response = {
    name: CLIENT_NAME,
    address: myAddress.ip + ':' + CLIENT_PORT
  }
  response = JSON.stringify(response)
  console.log(">> myAddress", myAddress)
  console.log(">> response(%d)", response.length, response)

  require('./socketServer').start(CLIENT_PORT)

  client.send(response, 0, response.length, rinfo.port, rinfo.address, function (err, bytes) {

    if (err) throw err;
    console.log('UDP message sent to ' + rinfo.address + ':' + rinfo.port);

  });
});

client.on("listening", function () {
  var address = client.address();
  console.log("client listening " + address.address + ":" + address.port);
});

client.bind(41234);

function getIPOnSourceIFace(sourceIP) {

  var ifaces = os.networkInterfaces();

  // best match is one with highest mask length, which is, the ip that looks most similar to source ip (Left2Right)
  var best = {
    mask: 0,
    ip: "",
    interface: ""
  }
  Object.keys(ifaces).forEach(function (ifname) {
    var alias = 0;

    ifaces[ifname].some(function (iface) {
      if ('IPv4' !== iface.family || iface.internal !== false) {
        // skip over internal (i.e. 127.0.0.1) and non-ipv4 addresses
        return;
      }
      let mask = guessMask(iface.address, sourceIP);
      if (best.mask < mask) {
        best.mask = mask;
        best.ip = iface.address;
        best.interface = ifname;
      }

      if (best.mask == 24) return true; // 24 is the max subnet mask you can have. Its no use to loop beyond 24. #micro-optimization
    });
  });

  return best;
}

function guessMask(ip1, ip2) {
  ip1 = ip1.split('.').map(part => parseInt(part))
  ip2 = ip2.split('.').map(part => parseInt(part))
  let score = 0;
  for (let i = 0; i < 4; ++i) {
    if (ip1[i] == ip2[i]) score++;
    else break;
  }
  return score * 8;
}

var cleanFileList = []
exports.rememberToClean = (filename) => {
  filename = path.resolve(filename);
  if(cleanFileList.indexOf(filename)>-1) return;
  cleanFileList.push(filename);
}
 exports.cleanFiles = () => {
   console.log(cleanFileList)
  cleanFileList.forEach( file => fs.unlinkSync(file) )
  cleanFileList = [];
}

// http://stackoverflow.com/a/14032965/2605574
function exitHandler(exceptions) {
	console.log("Time to sleep...",exceptions);
  try{
    exports.cleanFiles();
  } catch(e) {
    console.error(e);
  }
  process.exit();
}
//do something when app is closing
process.on('exit', exitHandler);

//catches ctrl+c event
process.on('SIGINT', exitHandler);

//catches uncaught exceptions
process.on('uncaughtException', exitHandler);
