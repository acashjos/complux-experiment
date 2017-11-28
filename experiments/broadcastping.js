var dgram = require('dgram');
var message = new Buffer("Some bytes");
var pulse = ()=>{
    var client = dgram.createSocket("udp4");
    client.on('listening', function(){
        client.setBroadcast(true);
    });
    client.bind();
    client.send(message, 0, message.length, 41234, "192.168.1.255", () => {
        client.close();
        setTimeout(pulse,5000)
    });
    console.log("sent!!")
}
pulse();