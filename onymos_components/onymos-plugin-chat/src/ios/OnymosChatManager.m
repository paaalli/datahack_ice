/*
 * Copyright 2015-2016 Onymos Inc
 *
 */

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>
#import <Cordova/CDVPluginResult.h> 
#import "OnymosChatManager.h"

@implementation OnymosChatManager

- (void) runAsBackground:(CDVInvokedUrlCommand*)command {
		[self.commandDelegate runInBackground:^{
			NSString* payload = nil;
			CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:payload];
			[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
		}];
	}

- (void) getApplicationName:(CDVInvokedUrlCommand*)command {		
		NSString* bundleIdentifier = [[NSBundle mainBundle] bundleIdentifier];
				
		if (bundleIdentifier != nil)
		{
			[self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:bundleIdentifier] callbackId:command.callbackId];
		}
		else
		{
			[self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:bundleIdentifier] callbackId:command.callbackId];
		}
	}

- (void) getApplicationKey:(CDVInvokedUrlCommand*)command {
		CDVPluginResult* pluginResult = nil;
		NSString* name = [command.arguments objectAtIndex:0];

		if (name != nil && [name length] > 0) {
			pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:name];
		}
		else {
			pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
		}

		[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
	}

- (void) getApplicationTimestamp:(CDVInvokedUrlCommand*)command {
		CDVPluginResult* pluginResult = nil;
		int min = [[command.arguments objectAtIndex:0] intValue];
		NSDateFormatter *Date = [[NSDateFormatter alloc] init];
		NSString* Name = nil;

		if (min > 0) {
			NSMutableDictionary *appDetail = [NSMutableDictionary dictionaryWithCapacity:2]; 
			[appDetail setObject:Name forKey:@"title"];
			[appDetail setObject:Date forKey:@"date"];
			pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:appDetail];
		}
		else {
			pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
		}

		[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
	}

- (void) copy:(CDVInvokedUrlCommand*)command {
		[self.commandDelegate runInBackground:^{
			UIPasteboard* pb = [UIPasteboard generalPasteboard];
			NSString* text = [command.arguments objectAtIndex:0];

			[pb setValue:text forPasteboardType:@"public.text"];

			CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"success"];
			[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
		}];
	}

- (void) paste:(CDVInvokedUrlCommand*)command {
		[self.commandDelegate runInBackground:^{
			UIPasteboard* pb = [UIPasteboard generalPasteboard];
			
			CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"success"];
			[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
		}];
	}

@end