/*
 * OnymosAccessManager.h
 * Onymos Access Component
 *
 * Copyright 2015-2017 Onymos Inc
 *
 * Use of Onymos Access Component is subject to the Onymos Terms of License Agreement
 *
 */
 
#import <Cordova/CDV.h>
#import <Cordova/CDVPlugin.h>
#import <GoogleSignIn/GoogleSignIn.h>

@interface OnymosAccessManager : CDVPlugin<GIDSignInDelegate, GIDSignInUIDelegate>

@property (nonatomic, copy) NSString* callbackId;
@property (nonatomic, assign) BOOL atLogin;

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

- (void) runAsBackground:(CDVInvokedUrlCommand*)command;
- (void) getApplicationName:(CDVInvokedUrlCommand*)command;
- (void) getApplicationKey:(CDVInvokedUrlCommand*)command;
- (void) getApplicationTimestamp:(CDVInvokedUrlCommand*)command;

- (void) googleLogin:(CDVInvokedUrlCommand*)command;
- (void) googleGetAuthData:(CDVInvokedUrlCommand*)command;
- (void) googleLogout:(CDVInvokedUrlCommand*)command;
- (void) googleDisconnect:(CDVInvokedUrlCommand*)command;

@end