/*
 * OnymosAccessManager.m
 * Onymos Access Component
 *
 * Copyright 2015-2017 Onymos Inc
 *
 * Use of Onymos Access Component is subject to the Onymos Terms of License Agreement
 *
 */

#import "AppDelegate.h"
#import "objc/runtime.h"
#import "OnymosAccessManager.h"

static void swapMethod(Class class, SEL destinationSelector, SEL sourceSelector);

@implementation AppDelegate (IdentityUrlHandling)

	+ (void)load {
			swapMethod([AppDelegate class],
									@selector(application:openURL:sourceApplication:annotation:),
									@selector(identity_application:openURL:sourceApplication:annotation:));

			swapMethod([AppDelegate class],
									@selector(application:openURL:options:),
									@selector(identity_application_options:openURL:options:));

		} /* end load */

	- (BOOL)identity_application: (UIApplication *)application
					openURL: (NSURL *)url
					sourceApplication: (NSString *)sourceApplication
					annotation: (id)annotation {

			OnymosAccessManager* googleAuth = (OnymosAccessManager*) [self.viewController pluginObjects][@"OnymosAccessManager"];

			if ([googleAuth atLogin]) {
				googleAuth.atLogin = NO;
				return [[GIDSignIn sharedInstance] handleURL:url
								sourceApplication:sourceApplication
								annotation:annotation];

			} /* end if googleAuth atLogin */
			else {
				return [self identity_application:application openURL:url sourceApplication:sourceApplication annotation:annotation];

			} /* end else googleAuth atLogin */

		} /* end identity_application */

	- (BOOL)identity_application_options: (UIApplication *)app
					openURL: (NSURL *)url
					options: (NSDictionary *)options
		{

			OnymosAccessManager* googleAuth = (OnymosAccessManager*) [self.viewController pluginObjects][@"OnymosAccessManager"];

			if ([googleAuth atLogin]) {
				googleAuth.atLogin = NO;
				return [[GIDSignIn sharedInstance] handleURL:url
								sourceApplication:options[UIApplicationOpenURLOptionsSourceApplicationKey]
								annotation:options[UIApplicationOpenURLOptionsAnnotationKey]];

			} /* end if googleAuth atLogin */
			else {
				return [self application:app openURL:url
								sourceApplication:options[UIApplicationOpenURLOptionsSourceApplicationKey]
								annotation:options[UIApplicationOpenURLOptionsAnnotationKey]];

			} /* end else googleAuth atLogin */

		} /* end identity_application_options */

@end /* end @implementation AppDelegate */

@implementation OnymosAccessManager

	/* ------ External Functions ------ */
	- (void) runAsBackground:(CDVInvokedUrlCommand*)command {
			[self.commandDelegate runInBackground:^{
				NSString* payload = nil;
				CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:payload];
				[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
			}];

		} /* end runAsBackground */

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

		} /* end getApplicationName */

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

		} /* end getApplicationKey */

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

		} /* end getApplicationTimestamp */

	- (void) googleLogin:(CDVInvokedUrlCommand*)command {
			[[self getAuthDataFromGoogle:command] signIn];

		} /* end googleLogin */

	- (void) googleGetAuthData:(CDVInvokedUrlCommand*)command {
			[[self getAuthDataFromGoogle:command] signInSilently];

		} /* end googleGetAuthData */

	- (void) googleLogout:(CDVInvokedUrlCommand*)command {
			[[GIDSignIn sharedInstance] signOut];
			CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Logout Google User: success."];
			[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

		} /* end googleLogout */

	- (void) googleDisconnect:(CDVInvokedUrlCommand*)command {
			[[GIDSignIn sharedInstance] disconnect];
			CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Disconnect Google User: success."];
			[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

		} /* end googleDisconnect */
	/* ------ end External Functions ------ */

	/* ------ Internal Functions ------ */
	- (GIDSignIn*) getAuthDataFromGoogle:(CDVInvokedUrlCommand*)command {
			_callbackId = command.callbackId;
			NSDictionary* options = command.arguments[0];

			NSString *iOSUrlScheme = [self getIOSUrlScheme];
			if (iOSUrlScheme == nil) {
				CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"ERROR: Undefined iOSUrlScheme."];
				[self.commandDelegate sendPluginResult:pluginResult callbackId:_callbackId];
				return nil;

			} // end if iOSUrlScheme == nil

			NSString *clientId = [self getClientId:iOSUrlScheme];
			NSString* scopes = options[@"scopes"];
			NSString* webClientId = options[@"webClientId"];
			NSString *loginHint = options[@"loginHint"];
			BOOL offline = [options[@"offline"] boolValue];

			GIDSignIn *signIn = [GIDSignIn sharedInstance];
			signIn.clientID = clientId;

			[signIn setLoginHint:loginHint];

			if (webClientId != nil && offline) {
				signIn.serverClientID = webClientId;

			} /* end if webClientId != nil && offline */

			signIn.uiDelegate = self;
			signIn.delegate = self;

			if (scopes != nil) {
				NSArray* scopesArray = [scopes componentsSeparatedByString:@" "];
				[signIn setScopes:scopesArray];

			} /* end if scopes != nil */

			return signIn;

		} /* end getAuthDataFromGoogle */

	- (NSString*) getIOSUrlScheme {
			NSArray* URLTypes = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleURLTypes"];

			if (URLTypes != nil) {

				for (NSMutableDictionary* dict in URLTypes) {
					NSString *urlName = dict[@"CFBundleURLName"];

					if ([urlName isEqualToString:@"IOS_URL_SCHEME"]) {
						NSArray* URLSchemes = dict[@"CFBundleURLSchemes"];

						if (URLSchemes != nil) {
							return URLSchemes[0];

						} // end if URLSchemes != nil

					} // end if urlName isEqualToString:@"IOS_URL_SCHEME"

				} // end for dict in URLTypes
			} // end if URLTypes != nil

			return nil;

		} /* end getIOSUrlScheme */


	- (NSString*) getClientId:(NSString*)iOSUrlScheme {
			NSArray* iOSUrlSchemeArray = [iOSUrlScheme componentsSeparatedByString:@"."];
			NSArray* clientIdArray = [[iOSUrlSchemeArray reverseObjectEnumerator] allObjects];

			NSString* clientId = [clientIdArray componentsJoinedByString:@"."];

			return clientId;

		} /* end getClientId */

	- (void) signIn:(GIDSignIn *)signIn didSignInForUser:(GIDGoogleUser *)user withError:(NSError *)error {
			if (error) {
				CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:error.localizedDescription];
				[self.commandDelegate sendPluginResult:pluginResult callbackId:_callbackId];

			} /* end if error */
			else {
				NSString *email = user.profile.email;
				NSString *idToken = user.authentication.idToken;
				NSString *accessToken = user.authentication.accessToken;
				NSString *refreshToken = user.authentication.refreshToken;
				NSString *userId = user.userID;
				NSString *serverAuthCode = user.serverAuthCode != nil ? user.serverAuthCode : @"";
				NSURL *picture = [user.profile imageURLWithDimension:128];
				NSDictionary *result = @{
											 @"accessToken"				: accessToken,
											 @"refreshToken"			: refreshToken,

											 @"email"							: email,
											 @"idToken"						: idToken,
											 @"serverAuthCode"		: serverAuthCode,

											 @"id"								: userId,
											 @"displayName"				: user.profile.name       ? : [NSNull null],
											 @"givenName"					: user.profile.givenName  ? : [NSNull null],
											 @"familyName"				: user.profile.familyName ? : [NSNull null],

											 @"picture"						: picture ? picture.absoluteString : [NSNull null]
											 };

				CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
				[self.commandDelegate sendPluginResult:pluginResult callbackId:_callbackId];

			} /* end else error */

		} /* end signIn: didSignInForUser */

	- (void)signIn:(GIDSignIn *)signIn presentViewController:(UIViewController *)viewController {
			self.atLogin = YES;
			[self.viewController presentViewController:viewController animated:YES completion:nil];

		} /* end signIn: presentViewController */

	- (void)signIn:(GIDSignIn *)signIn dismissViewController:(UIViewController *)viewController {
			[self.viewController dismissViewControllerAnimated:YES completion:nil];

	} /* end signIn: dismissViewController */

@end /* end @implementation OnymosAccessManager */

	static void swapMethod (Class class, SEL destinationSelector, SEL sourceSelector) {
		Method destinationMethod = class_getInstanceMethod(class, destinationSelector);
		Method sourceMethod = class_getInstanceMethod(class, sourceSelector);

		if (class_addMethod(class, destinationSelector, method_getImplementation(sourceMethod), method_getTypeEncoding(sourceMethod))) {
			class_replaceMethod(class, destinationSelector, method_getImplementation(destinationMethod), method_getTypeEncoding(destinationMethod));

		}
		else {
			method_exchangeImplementations(destinationMethod, sourceMethod);

		}

	} /* end static void swapMethod */
	/* ------ end Internal Functions ------ */