import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { switchMap } from 'rxjs';
import {AuthService} from "../../../../service/auth.service";
import {FormsModule} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {CommonModule} from "@angular/common";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";

@Component({
  selector: 'app-reset',
  templateUrl: './reset.component.html',
  standalone: true,
  imports: [FormsModule,CommonModule,MatInputModule,MatButtonModule,MatFormFieldModule],
  styleUrls: ['./reset.component.css']
})
export class ResetComponent implements OnInit {
 password?: string;
 passwordCheck?: string;
 token?: string | null;


 constructor(
  private authService: AuthService,
  private snackBar: MatSnackBar,
  private router: Router,
  private route: ActivatedRoute,

) {}


  ngOnInit(): void {
  }


  reset() {
    if (this.password !== this.passwordCheck) {
      this.snackBar.open("Le password non corrispondono", 'Errore');
      return;
    }
    this.route.paramMap.pipe(
      switchMap((params) => {
        this.token = params.get('token');
        return this.authService.changePass(this.password!, this.token!, 'body');
      })
    ).subscribe(
      (data) => {
        console.log("GG");
        this.snackBar.open("Password cambiata con successo", 'OK');
        this.router.navigate(['/login']);
      },
      (error) => {
        console.error('Errore durante il reset della password:', error);
        this.snackBar.open("Errore durante il reset della password", 'Errore');
      }
    );
  }
}

