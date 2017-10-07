import { ChangeDetectorRef, Component } from '@angular/core';

import { NavController } from 'ionic-angular';

import { Login } from '../login/login';
import { InviteDetails } from '../invite-details/invite-details';

declare var OnymosAccess:any;
declare var OnymosContacts:any;
declare var OnymosContactsConstants:any;

@Component({
	selector: 'page-invite',
	templateUrl: 'invite.html'
})

export class Invite {

	authDataObject: any;
	errorMessage: string = '';
	contacts: Array<any> = [];
	loadingContacts: boolean = false;

	constructor (	public navCtrl: NavController,
								private cdRef: ChangeDetectorRef) {

		this.authDataObject = OnymosAccess.getAuth();

		if (this.authDataObject) {
			this.updateContactsList(null);

		}

	} /* end constructor */

	ionViewCanEnter(): boolean {
		if (OnymosAccess.getAuth()) {
			return true;
		}
		else {
			return false;
		}

	} /* end ionViewCanEnter */

	updateContactsList (searchEvent: any) {

		let searchFilter = '';
		if (searchEvent) searchFilter = searchEvent.target.value;

		let searchOptions = {
			filter: searchFilter,
			hasPhoneNumber: false,
			searchField: OnymosContactsConstants.SearchField.givenName,
			displayFields: [ 
				OnymosContactsConstants.DisplayFields.name, 
				OnymosContactsConstants.DisplayFields.nickname, 
				OnymosContactsConstants.DisplayFields.phoneNumbers, 
				OnymosContactsConstants.DisplayFields.emails, 
				OnymosContactsConstants.DisplayFields.addresses,
				OnymosContactsConstants.DisplayFields.photos, 
				OnymosContactsConstants.DisplayFields.note 
			], 
			numberOfRecords: 25
		};

		this.contacts = [];
		this.loadingContacts = true;

		let that = this;

		OnymosContacts.getList('0',
			function (contactsList) {
				that.contacts = contactsList;
				that.loadingContacts = false;

				that.cdRef.detectChanges();

			},
			function (error) {
				console.log('invite.ts : onymosContactsGetList error - ' + JSON.stringify(error));
				that.errorMessage = 'ERROR : Failed searching for Contacts.';
				that.loadingContacts = false;

				return;

			},
			searchOptions); /* end OnymosContacts.getList */


	} /* end updateContactsList */

	loadMoreContacts (loadMoreEvent: any) {
		this.loadingContacts = true;

		let that = this;

		OnymosContacts.loadNextSet('0',
			function (nextSet) {

				if (!nextSet || !nextSet.length || nextSet.length < 1) {
					that.loadingContacts = false;

					loadMoreEvent.complete();

					that.cdRef.detectChanges();
				}
				else {
					that.contacts.push.apply(that.contacts, nextSet);
					that.loadingContacts = false;

					loadMoreEvent.complete();

					that.cdRef.detectChanges();

					return;
				}

			},
			function (error) {
				console.log('invite.ts : onymosContactsLoadNextSet error - ' + error);
				that.errorMessage = 'ERROR : Failed loading more Contacts.';
				that.loadingContacts = false;

				loadMoreEvent.complete();

				return;
			}); /* end OnymosContacts.loadNextSet */

	} /* end loadMoreContacts */

	messageInvitee (contact) {
		if (!contact.phoneNumbers && !contact.emails) return;

		this.navCtrl.push(InviteDetails, {
			contact: contact
		})
		.catch(() => {

			// Page requires authentication, re-direct to Login page
			this.navCtrl.setRoot(Login, {routeToPage: 'Invite'});

		});

	} /* end messageInvitee */

} /* end export class Invite */
