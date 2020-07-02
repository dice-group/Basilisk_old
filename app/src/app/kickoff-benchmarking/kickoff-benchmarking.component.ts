import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-kickoff-benchmarking',
  templateUrl: './kickoff-benchmarking.component.html',
  styleUrls: ['./kickoff-benchmarking.component.css']
})
export class KickoffBenchmarkingComponent implements OnInit {

  constructor() { }

  @ViewChild('username', {static: false}) username: ElementRef;
  @ViewChild('password', {static: false}) password: ElementRef;
  @ViewChild('hook', {static: false}) hook: ElementRef;

  url1 = "http://131.234.28.165:8080/runbenchmark?userName="
  url2 = "&password="
  url3 = "&hook="

  ngOnInit(): void {
  }

  start() {
    var hookid = this.hook.nativeElement.selectedIndex + 1;
    var url=this.url1 + this.username.nativeElement.value + this.url2 + this.password.nativeElement.value + this.url3 + hookid;
    window.open(url, '_blank');
  }

}
