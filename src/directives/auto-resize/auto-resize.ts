import { Directive, HostListener, ElementRef } from "@angular/core";

@Directive({
	selector: "ion-textarea[auto-resize]"
})
export class AutoResize {

	@HostListener("input", ["$event.target"])
	onInput(textArea: HTMLTextAreaElement): void {
		this.resize();
	}

	constructor(public element: ElementRef) {
	}

	ngOnInit(): void {
		this.resize();
	}

	resize(): void {
		let textAreaToResize = this.element.nativeElement.querySelector("textarea");

		if (textAreaToResize) {
			textAreaToResize.style.overflow = "hidden";
			textAreaToResize.style.height = "auto";
			textAreaToResize.style.height = textAreaToResize.scrollHeight + "px";
		} /* end if textAreaToResize */

	} /* end resize */

} /* end export class AutoResize */