const streamy = require("streamyjs");
const fs = require('fs');
const path = require('path');
let wallpaper;
let appList;
let deviceInfo = {};

function addToDesktop(app) {
	let appDisplayName = `${app.name} - ${deviceInfo.device}`
	let StartupWMClass = `${app.id}-${deviceInfo.device}`.replace(/\s/g,'-');
	let template = `#!/usr/bin/env xdg-open
[Desktop Entry]
Version=1.0
Terminal=false
Type=Application
Name=${appDisplayName}
Exec=/opt/google/chrome/google-chrome --app=http://localhost:${process.env.UI_PORT}/${app.id}/
Icon=${app.icon}
StartupWMClass=${StartupWMClass}`

console.log(`Debug in browser: http://localhost:${process.env.UI_PORT}/${app.id}/` )
let desktopPath = path.resolve(process.env.HOME+"/.local/share/applications/" + StartupWMClass + '.desktop');
				fs.writeFile(desktopPath, template, function (err) {
					if (err) {
						return console.log(err,iconpath);
					}

					console.log("Desktop file was saved!");
				});
				return desktopPath;
}

exports.init = (emit, cb) => {

	emit(null,"getWallpaper", null, onWallLoaded)

	function onWallLoaded(bin) {
		console.log("wallpaper", bin)
		wallpaper = bin;
		emit(null,"listApps", null, onAppListLoaded)
	}

	function onAppListLoaded(applist) {
		console.log("applist", applist)
		appList = JSON.parse(applist);

		let walker = streamy(Object.keys(appList)).forEach(function (appId) {
			emit(appId,"getIcon", null, (bin) => {
				let iconpath = path.resolve("tmp/" + appId + '.png');
				fs.writeFile(iconpath, bin, function (err) {
					if (err) {
						return console.log(err,iconpath);
					}

					console.log("The file was saved!");
				});
				appList[appId] = {
					name: appList[appId],
					icon: iconpath,
					id: appId
				};
console.log(`Debug in browser: http://localhost:${process.env.UI_PORT}/${appList[appId].id}/` )
				
				let desktopFile = addToDesktop(appList[appId]);
				require('./index').rememberToClean(desktopFile)
				require('./index').rememberToClean(iconpath)
				if (walker.isMoving()) walker.walk();
				else if (cb) cb()
			})
		});

		walker.walk();
	}
}

exports.getContext = () => ({
	wallpaper,
	appList,
	get deviceName() { return deviceInfo.device || "Unnamed Device" }
})

// exports.updateState = (updates) => {
// 	Object.keys(updates).forEach(
// 		key => {
// 			switch (key) {
// 				case "device": deviceName = updates[key]
// 			}
// 		}
// 	)
// }

exports.setDevice = (deviceInf) => {
	deviceInfo = deviceInf;
}