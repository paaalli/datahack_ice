import {Injectable} from '@angular/core';

@Injectable()
export class OnymosServices {
	constructor() {}

	formatTime (timestamp) {

		let currentTime : any = new Date();
		let messageTime : any = new Date(timestamp);

		let timeDiff : any = currentTime - messageTime;

		if (timeDiff <= 86400*1000) {
			let formattedTime_Hours : any = messageTime.getHours() <= 12 ? messageTime.getHours() : messageTime.getHours() - 12;
				if (formattedTime_Hours === 0) formattedTime_Hours = '0' + formattedTime_Hours;
			let formattedTime_Minutes : string = messageTime.getMinutes()  < 10 ? '0' + messageTime.getMinutes() : messageTime.getMinutes();
			let formattedTime_AMPM : string = messageTime.getHours() < 12 ? 'AM' : 'PM';

			return formattedTime_Hours + ':' + formattedTime_Minutes + ' ' + formattedTime_AMPM;

		} // end if timeDiff <= 86400*1000
		else {
			let numberOfDays : number = Math.floor(timeDiff / (86400*1000));
			let days : string[] = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
			let months : string[] = ['Jan.', 'Feb.', 'Mar.', 'Apr.', 'May', 'Jun.', 'Jul.', 'Aug.', 'Sep.', 'Oct.', 'Nov.', 'Dec.'];

			if (numberOfDays === 1) return 'Yesterday';
			if (numberOfDays > 1 && numberOfDays < 7) return days[messageTime.getDay()];

			return months[messageTime.getMonth()] + ' ' + messageTime.getDate() + ', ' + messageTime.getFullYear();

		} // end else timeDiff <= 86400*1000

	} // end function formatTime

} /* end export class OnymosServices */