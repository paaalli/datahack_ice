import { ChangeDetectorRef, Component } from '@angular/core';

import { NavController, NavParams } from 'ionic-angular';
import { OnymosServices } from '../../services/onymos-services';

declare var OnymosAccess:any;
declare var OnymosDigiMe:any;

@Component({
	selector: 'page-digime-details',
	templateUrl: 'digime-details.html',
	providers: [OnymosServices]
})
export class DigiMeDetails {

	errorMessage: string = '';
	fileName: string = '';

	displayGetDetailsSpec: boolean = false;
	getDetailsQueryInProgress: boolean = false;
	getDetailsQueryComplete: boolean = false;

	fileData: string = '';

	constructor (	public navCtrl: NavController,
								public navParams: NavParams,
								public onymosServices: OnymosServices,

								private cdRef: ChangeDetectorRef) {

		let fileNameObject = navParams.data;
		this.fileName = fileNameObject.fileName;

		console.log('DEBUG : fileName [' + this.fileName + ']');

	} /* end constructor */

	ionViewCanEnter(): boolean {
		if (OnymosAccess.getAuth()) {
			return true;
		}
		else {
			return false;
		}

	} /* end ionViewCanEnter */

	getFileDetails (fileName) {
		this.fileData = '';

		this.errorMessage = '';

		this.getDetailsQueryInProgress = true;
		this.getDetailsQueryComplete = false;

		let inputObject = {
			fileName: fileName
		};

		let that = this;

		OnymosDigiMe.getDetails(inputObject,
			function getDetailSuccess (retrievedFileData) {
				console.log('DEBUG : getDetail Output : [' + JSON.stringify(retrievedFileData) + ']');

				console.log('DEBUG : getDetail Content Object [' + retrievedFileData.content + ']');
				console.log('DEBUG : getDetail Content Stringify [' + JSON.stringify(retrievedFileData.content) + ']');

				//that.fileData = retrievedFileData.content.replace(/,/g, '<BR><BR>').replace(/{/g, '<ul>').replace(/}/g, '</ul>').replace(/\"/g, '');

				that.fileData = '<TABLE><TR><TD>' +
					retrievedFileData.content
						.substring(1, (retrievedFileData.content.length - 1))
						.replace(/,/g, '</TD></TR><TR><TD>')
						.replace(/:/g, '</TD><TD>')
						.replace(/{/g, '</TD><TD>&nbsp;</TD><TD>')
						.replace(/}/g, '<BR>')
						.replace(/\"/g, '') +
					'</TD></TR></TABLE>';

				console.log('DEBUG : getDetail fileData [' + that.fileData + ']');

				that.getDetailsQueryInProgress = false;
				that.getDetailsQueryComplete = true;

				that.cdRef.detectChanges();
				
			},
			function getDetailFailure (getDetailError) {
				console.log("DEBUG : JS : [" + getDetailError + "]");

				that.getDetailsQueryInProgress = false;
				that.getDetailsQueryComplete = true;

				that.cdRef.detectChanges();
		});

	}	// end function getFileDetails

} /* end export class DigiMeDetails */
