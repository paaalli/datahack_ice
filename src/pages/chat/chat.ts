import { ChangeDetectorRef, Component } from '@angular/core';

import { NavController } from 'ionic-angular';
import { OnymosServices } from '../../services/onymos-services';

import { Login } from '../login/login';
import { ChatDetails } from '../chat-details/chat-details';

declare var OnymosAccess:any;
declare var OnymosChat:any;

@Component({
	selector: 'page-chat',
	templateUrl: 'chat.html',
	providers: [OnymosServices]
})
export class Chat {

	authDataObject: any;
	errorMessage: string = '';
	chatList: Array<{
		messageId: string,
		sender: any,
		recipients: any,
		chatParticipants: string,
		messageContent: string,
		messageCreated: Date
	}> = [];


	constructor (	public navCtrl: NavController,
								public onymosServices: OnymosServices,
								private cdRef: ChangeDetectorRef) {

		this.authDataObject = OnymosAccess.getAuth();

		if (this.authDataObject) {
			let that = this;

			OnymosChat.stopListenForList('@AUTHID@');

			OnymosChat.listenForList(
				'@AUTHID@',

				function getListSuccess (chatSummary) {
					let newChatObject : any = {};

					newChatObject.messageId = chatSummary.messageId;
					newChatObject.sender = chatSummary.sender;
					newChatObject.recipients = chatSummary.recipients;

					newChatObject.chatParticipants =
						that.getFormattedParticipantList(chatSummary.sender, chatSummary.recipients);

					if (chatSummary.messageContent === '') {
						newChatObject.messageContent = '[media]';
					}
					else {
						newChatObject.messageContent = chatSummary.messageContent;
					}

					newChatObject.messageCreated = onymosServices.formatTime(chatSummary.messageCreated);

					that.chatList.push(newChatObject);
					that.cdRef.detectChanges();

				},

				function getListFailure (error) {
					console.log('ERROR : chat.ts : onymosChatListenForList failed with error - ' + error);
					that.errorMessage = 'Failed obtaining Chat List.';
					that.cdRef.detectChanges();
				}); /* end OnymosChat.listenForList */

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

	compose() {
		this.navCtrl.push(ChatDetails, {})
		.catch(() => {
			// Page requires authentication, re-direct to Login page
			this.navCtrl.setRoot(Login, {routeToPage: 'Chat'});

		});

	} /* end compose */

	viewChatDetails (sender, recipients) {
		this.navCtrl.push(ChatDetails, {
			sender: sender,
			recipients: recipients
		})
		.catch(() => {

			// Page requires authentication, re-direct to Login page
			this.navCtrl.setRoot(Login, {routeToPage: 'Chat'});

		});

	} /* end viewChatDetails */

	getFormattedParticipantList (sender, recipients) {
		let participantsString : string = '';

		if (!sender) {
			this.errorMessage = 'ERROR : Missing Sender info.';
			return;

		} /* end if !recipients || recipients.length === 0 */

		if (!recipients || recipients.length === 0) {
			this.errorMessage = 'ERROR : Missing Recipients info.';
			return;

		} /* end if !recipients || recipients.length === 0 */

		if (sender.userId !== this.authDataObject.userId) {
			participantsString = sender.userName;
		}

		for (let i = 0; i < recipients.length; i++) {

			if (recipients[i].userId !== this.authDataObject.userId) {
				if (participantsString === '') {
					participantsString = recipients[i].userName;
				}
				else {
					participantsString = participantsString + ', ' + recipients[i].userName;
				}
			} /* end if recipients[i].userId !== this.authDataObject.userId */

		} /* end for i < recipients.length */

		// Replace last comma with & sign
		participantsString = participantsString.replace(/\,([^\,]*)$/,' \& '+'$1');

		return participantsString;
	}; /* end function getFormattedParticipantList */

} /* end export class Chat */
