#!/usr/bin/env node
'use strict';

var fs = require('fs');

var getValue = function(config, name) {
    var value = config.match(new RegExp('<' + name + '>(.*?)</' + name + '>', "i"))
    if(value && value[1]) {
        return value[1]
    } else {
        return null
    }
}

function fileExists(path) {
  try  {
    return fs.statSync(path).isFile();
  }
  catch (e) {
    return false;
  }
}

function directoryExists(path) {
  try  {
    return fs.statSync(path).isDirectory();
  }
  catch (e) {
    return false;
  }
}


if(fileExists("digime2y.p12") && directoryExists("platforms/android/assets")){
  try {
    var contents = fs.readFileSync('digime2y.p12');
    fs.writeFileSync("platforms/android/assets/digime2y.p12", contents);
  } catch(err) {
    process.stdout.write(err);
  }
}

if(fileExists("digime3m.p12") && directoryExists("platforms/android/assets")){
  try {
    var contents = fs.readFileSync('digime3m.p12');
    fs.writeFileSync("platforms/android/assets/digime3m.p12", contents);
  } catch(err) {
    process.stdout.write(err);
  }
}




