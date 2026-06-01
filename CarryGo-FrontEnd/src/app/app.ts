import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

// Root component. The <router-outlet> inside app.html is where Angular
// renders whichever page matches the current URL.
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {
  protected readonly title = signal('CarryGo-FrontEnd');
}
