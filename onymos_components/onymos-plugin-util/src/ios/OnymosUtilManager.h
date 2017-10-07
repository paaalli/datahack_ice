/*
 * Copyright 2015-2016 Onymos Inc
 *
 */
 
#import <Cordova/CDV.h>
#import <Cordova/CDVPlugin.h>
#import <Foundation/Foundation.h>

@interface OnymosUtilManager : CDVPlugin <UIDocumentMenuDelegate,UIDocumentPickerDelegate>

enum CDVType {
    InUse = 0,
    Idle,
    Sleep
};
typedef NSUInteger CDVType;

enum CDVVersion {
  VersionPrev = 0,
  VersionNext
};
typedef NSUInteger CDVVersion;

enum CDVQualityType {
  HighQuality = 0,
  MediumQuality = 1,
  LowQuality = 2,
};
typedef NSUInteger CDVQualityType;

enum CDVOutputType {
  VER = 0,
  SER = 1,
  TER = 2,
  MER = 3
};

@property (strong, nonatomic) CDVPluginResult * pluginResult;
@property (strong, nonatomic) CDVInvokedUrlCommand * command;


- (void)runAsBackground:(CDVInvokedUrlCommand*)command;
- (void)getApplicationName:(CDVInvokedUrlCommand*)command;
- (void)getApplicationKey:(CDVInvokedUrlCommand*)command;
- (void)getApplicationTimestamp:(CDVInvokedUrlCommand*)command;
- (void)isAvailable:(CDVInvokedUrlCommand*)command;
- (void)selectFile:(CDVInvokedUrlCommand*)command;

@end