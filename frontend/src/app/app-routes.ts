import { Routes } from '@angular/router';
import { HostView } from './host-view/host-view';
import { GuestView } from './guest-view/guest-view';
import { HostComponent } from './host-component/host-component';

export const routes: Routes = [
    { path: '', component: HostView, title: 'TransferNow | Your Files' },
    { path: 'requestUpload', component: GuestView, title: 'TransferNow | Request Upload' },
    { path: 'checkRequests', component: HostComponent, title: 'TransferNow | Check Requests' },
];