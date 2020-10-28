import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { MatSelectModule } from '@angular/material/select';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule} from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MenuComponent } from './menu/menu.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DEFAULT_OPTIONS } from '@angular/material/dialog';
import { Ng5SliderModule } from 'ng5-slider';
import { KickoffBenchmarkingComponent } from './kickoff-benchmarking/kickoff-benchmarking.component';



@NgModule({
  declarations: [
    AppComponent,
    MenuComponent,
    KickoffBenchmarkingComponent
  ],
  entryComponents: [
    KickoffBenchmarkingComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatSelectModule,
    DragDropModule,
    MatMenuModule,
    MatButtonModule,
    MatIconModule,
    CommonModule,
    FormsModule,
    MatDialogModule,
    Ng5SliderModule
  ],
  providers: [{provide: MAT_DIALOG_DEFAULT_OPTIONS, useValue: {hasBackdrop: false}}],
  bootstrap: [AppComponent]
})
export class AppModule { }
