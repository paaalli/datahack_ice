import { Component } from '@angular/core';

import { NavController } from 'ionic-angular';
import { OnymosServices } from '../../services/onymos-services';

import { Home } from '../home/home';

declare var OnymosAccess:any;
declare var OnymosContacts:any;

@Component({
	selector: 'page-account',
	templateUrl: 'account.html',
	providers: [OnymosServices]
})
export class Account {

	authDataObject: any;
	errorMessage: string = '';

	userName: string = '';
	userPhoto: string = '';
	authProvider: string = '';

	constructor (	public navCtrl: NavController,
								public onymosServices: OnymosServices) {

		this.authDataObject = OnymosAccess.getAuth();

		if (this.authDataObject) {
			this.userName = this.authDataObject.userName;
			this.userPhoto = this.authDataObject.userPhoto;
			this.authProvider = this.authDataObject.authProvider;

		} /* end if this.authDataObject */

	} /* end constructor */

	ionViewCanEnter(): boolean {
		if (OnymosAccess.getAuth()) {
			return true;
		}
		else {
			return false;
		}

	} /* end ionViewCanEnter */

	socialLogout() {
		let that = this;

		OnymosAccess.logout(
			function logoutSuccess (message) {
				that.errorMessage = '';
				that.navCtrl.setRoot(Home);

				return;

			}, // end function logoutSuccess
			function logoutFailure (error) {
				console.log('ERROR : account.ts : onymosAccessLogout failed with error - ' + error);
				that.errorMessage = 'Failed in Logout.';

				return;

		}); // end function logoutFailure

	} /* end function socialLogout */

} /* end export class Account */
