import { Component } from '@angular/core';

import { NavController, NavParams } from 'ionic-angular';
import { Events } from 'ionic-angular';

import { Account } from '../account/account';
import { Chat } from '../chat/chat';
import { DigiMe } from '../digime/digime';
import { Invite } from '../invite/invite';

declare var OnymosAccess:any;

@Component({
	selector: 'page-login',
	templateUrl: 'login.html'
})

export class Login {
	routeToPage: any;

	constructor (	public events: Events,
								public navCtrl: NavController,
								public navParams: NavParams) {
		this.routeToPage = navParams.get('routeToPage') || '';

		if (OnymosAccess.getAuth()) {
			events.publish('user:login', Date.now());

			switch (this.routeToPage.toLowerCase()) {
				case 'account':
					this.navCtrl.setRoot(Account);
					break;

				case 'chat':
					this.navCtrl.setRoot(Chat);
					break;

				case 'digime':
					this.navCtrl.setRoot(DigiMe);
					break;

				case 'invite':
					this.navCtrl.setRoot(Invite);
					break;

				default:
					this.navCtrl.setRoot(Chat);

			} /* end switch this.routeToPage */


		} /* end if OnymosAccess.getAuth() */
		else {
			console.log('Not Logged in. Displaying Login page.');
		}

	} /* end constructor */

	socialLogin(authProvider) {

		let accessObj = { 
			authProvider : authProvider
		};

		let navCtrl = this.navCtrl;
		let routeToPage = this.routeToPage || '';
		let events = this.events;

		OnymosAccess.login(accessObj,
			function onymosAccessLoginSuccess (data) {
				console.log('login.ts : onymosAccessLogin - Success');

				let authDataObject = OnymosAccess.getAuth();

				if (authDataObject) {

					// Publish User Login event
					events.publish('user:login', Date.now());

					switch (routeToPage.toLowerCase()) {
						case 'account':
							navCtrl.setRoot(Account);
							break;

						case 'chat':
							navCtrl.setRoot(Chat);
							break;

						case 'digime':
							navCtrl.setRoot(DigiMe);
							break;

						case 'invite':
							navCtrl.setRoot(Invite);
							break;

						default:
							navCtrl.setRoot(Chat);

					} /* end switch this.routeToPage */

					return;
				} /* end if authDataObject */
				else {
					console.log('login.ts : onymosAccessGetAuth - Returned null.');

				} /* end else authDataObject */

			}, /* end function onymosAccessLoginSuccess */
			function onymosAccessLoginFailure (error) {
				console.log('login.ts : onymosAccessLogin error - ' + error);
				return;

			}); /* end function onymosAccessLoginFailure */

	} /* end function socialLogin */

} /* end export class Login */
