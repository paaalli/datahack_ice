/*
 * Copyright 2015-2016 Onymos Inc
 *
 */
 
#import "OnymosUtilManager.h"

@implementation OnymosUtilManager 

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

- (void) isAvailable:(CDVInvokedUrlCommand*)command {
		BOOL supported = NSClassFromString(@"UIDocumentPickerViewController");
		[self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:supported] callbackId:command.callbackId];
	}

- (void) selectFile:(CDVInvokedUrlCommand*)command {

	self.command = command;id U544973=[command.arguments objectAtIndex:0];BOOL s7570706f72746564=YES;NSArray * U5449734172726179=nil;CGRect f72616d65=CGRectZero;
	if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {if(command.arguments.count > 1) {NSDictionary * f72616d6556616c=[command.arguments objectAtIndex:1];f72616d6556616c=[f72616d6556616c isEqual:[NSNull null]]?nil:f72616d6556616c;
	if(f72616d6556616c) {f72616d65.origin.x=[[f72616d6556616c valueForKey:@"x"] integerValue];f72616d65.origin.y=[[f72616d6556616c valueForKey:@"y"] integerValue];f72616d65.size.width=[[f72616d6556616c valueForKey:@"width"] integerValue];f72616d65.size.height=[[f72616d6556616c valueForKey:@"height"] integerValue];
	}}}
	if([U544973 isEqual:[NSNull null]]) {U5449734172726179=@[@"public.data"];} else if ([U544973 isKindOfClass:[NSString class]]){
	U5449734172726179=@[U544973];} else if([U544973 isKindOfClass:[NSArray class]]){U5449734172726179 = U544973;} else {s7570706f72746564=NO;[self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Unsupported Location"] callbackId:self.command.callbackId];}
	if(!NSClassFromString(@"UIDocumentPickerViewController")) {s7570706f72746564=NO;[self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"FileSelector unavailable"] callbackId:self.command.callbackId];}
	if(s7570706f72746564) {self.pluginResult=[CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];[self.pluginResult setKeepCallbackAsBool:YES];[self displayDocumentPicker:U5449734172726179 withSenderRect:f72616d65];}		
}

-(void) documentMenu:(UIDocumentMenuViewController *)documentMenu didPickDocumentPicker:(UIDocumentPickerViewController *)documentPicker {
		
	documentPicker.delegate = self;
	documentPicker.modalPresentationStyle = UIModalPresentationFullScreen;
	[self.viewController presentViewController:documentPicker animated:YES completion:nil];    
}

-(void) documentMenuWasCancelled:(UIDocumentMenuViewController *)documentMenu {

	self.pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"canceled"];
	[self.pluginResult setKeepCallbackAsBool:NO];
	[self.commandDelegate sendPluginResult:self.pluginResult callbackId:self.command.callbackId];    
}

- (void) documentPicker:(UIDocumentPickerViewController *)controller didPickDocumentAtURL:(NSURL *)url {
	[url startAccessingSecurityScopedResource];
	NSFileCoordinator *c6f6f7264696e61746f72=[[NSFileCoordinator alloc] init];NSError *e727255524c52656164;
	[c6f6f7264696e61746f72 coordinateReadingItemAtURL:url options:NSFileCoordinatorReadingForUploading error:&e727255524c52656164 byAccessor:^(NSURL *n657755524c) {
	NSString *f4e616d65=[n657755524c lastPathComponent];NSString *f457874=[n657755524c pathExtension];NSString *l6f63616c4650=[url absoluteString];
	if([f457874 isEqualToString:@"zip"]) {f4e616d65 = [[n657755524c URLByDeletingPathExtension] lastPathComponent];f457874 = [[n657755524c URLByDeletingPathExtension] pathExtension];NSError *e72724652656164;
	NSData *data = [[NSData alloc] initWithContentsOfURL:n657755524c options:0 error:&e72724652656164];NSLog(@"Error reading file %@", e72724652656164);
	NSString *t656d70446972 = NSTemporaryDirectory();NSString *l6f63616c436f70794650=[t656d70446972 stringByAppendingPathComponent:f4e616d65];
	NSError *e7272465772697465;if(![data writeToFile:l6f63616c436f70794650 options:NSDataWritingAtomic error:&e7272465772697465]) {NSLog(@"Error writing file %@", e7272465772697465);}l6f63616c4650 = [@"file://" stringByAppendingString:l6f63616c436f70794650];
	}
	NSMutableDictionary* u726c4f626a = [NSMutableDictionary dictionaryWithCapacity:4];[u726c4f626a setObject:l6f63616c4650 forKey:@"fileURI"];[u726c4f626a setObject:f457874 forKey:@"contentType"];
	self.pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:u726c4f626a];[self.pluginResult setKeepCallbackAsBool:NO];[self.commandDelegate sendPluginResult:self.pluginResult callbackId:self.command.callbackId];
	}];
	[url stopAccessingSecurityScopedResource];
}

- (void) documentPickerWasCancelled:(UIDocumentPickerViewController *)controller {
		
	self.pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"canceled"];
	[self.pluginResult setKeepCallbackAsBool:NO];
	[self.commandDelegate sendPluginResult:self.pluginResult callbackId:self.command.callbackId];
		
}

- (void) displayDocumentPicker:(NSArray *)U544973 withSenderRect:(CGRect)senderFrame{
		
	UIDocumentMenuViewController *importMenu = [[UIDocumentMenuViewController alloc] initWithDocumentTypes:U544973 inMode:UIDocumentPickerModeImport];

	importMenu.delegate = self;
	importMenu.popoverPresentationController.sourceView = self.viewController.view;
	if (!CGRectEqualToRect(senderFrame, CGRectZero)) {
			importMenu.popoverPresentationController.sourceRect = senderFrame;
	}
	[self.viewController presentViewController:importMenu animated:YES completion:nil];    
}

@end