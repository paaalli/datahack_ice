import { Component, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { Content, NavController, NavParams, Platform } from 'ionic-angular';

declare var OnymosAccess:any;

@Component({
	selector: 'page-invite-details',
	templateUrl: 'invite-details.html'
})

export class InviteDetails {
	@ViewChild(Content) content: Content;

	authDataObject: any;
	errorMessage: string = '';
	invitee: any;
	platform: any;

	constructor (	public navCtrl: NavController,
								public navParams: NavParams,
								platform: Platform,
								private sanitizer: DomSanitizer) {

		this.authDataObject = OnymosAccess.getAuth();
		this.invitee = navParams.data.contact;
		this.platform = platform;


	} /* end constructor */

	ionViewCanEnter(): boolean {
		if (OnymosAccess.getAuth()) {
			return true;
		}
		else {
			return false;
		}

	} /* end ionViewCanEnter */

	getSMSHrefForPhoneNumber (phoneNumber) {
		let formattedPhoneNumber = phoneNumber.replace(/[\(\)\+\-\.\,\_\s]*/g, '');
		let stringForSMSHref = '';

		if (this.platform.is('ios')) {
			stringForSMSHref = 'sms:' + formattedPhoneNumber +
				'&body=Check out ' + 'Your Appname' + '.%0D%0A%0D%0A' + 
				'iOS link: https://itunes.apple.com/us/app/yourappname/id123456789 %0D%0A%0D%0A' + 
				'(or)%0D%0A%0D%0A' +
				'Android link: https://play.google.com/store/apps/details?id=com.yourappname';

		}
		else {
			stringForSMSHref = 'sms:' + formattedPhoneNumber +
				'?body=Check out ' + 'Your Appname' + '.%0D%0A%0D%0A' + 
				'iOS link: https://itunes.apple.com/us/app/yourappname/id123456789 %0D%0A%0D%0A' + 
				'(or)%0D%0A%0D%0A' +
				'Android link: https://play.google.com/store/apps/details?id=com.yourappname';
		}

		return this.sanitizer.bypassSecurityTrustUrl(stringForSMSHref);

	} /* end function getSMSHrefForPhoneNumber */

	getEmailHrefForEmail (email) {

		let stringForEmailHref = 'mailto:' + email +
			'?subject=Check out ' + 'Your Appname' + '&body=Hi,%0D%0A Check out this ' + 'Your Appname ' + ' App.%0D%0A%0D%0A' +
			'iOS link: https://itunes.apple.com/us/app/yourappname/id123456789 %0D%0A%0D%0A' +
			'Android link: https://play.google.com/store/apps/details?id=com.yourappname %0D%0A%0D%0A%0D%0A' +
			this.authDataObject.userName;

		return this.sanitizer.bypassSecurityTrustUrl(stringForEmailHref);

	} /* end function getEmailHrefForEmail */

} /* end export class InviteDetails */
