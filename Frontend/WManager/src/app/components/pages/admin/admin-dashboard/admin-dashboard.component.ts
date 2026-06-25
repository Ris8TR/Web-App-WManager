import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserDto } from "../../../../model/userDto";
import { InterestAreaDto } from "../../../../model/interestAreaDto";
import { SensorDto } from "../../../../model/sensorDto";
import { SensorService } from "../../../../service/sensor.service";
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatRadioModule } from '@angular/material/radio';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { UserService } from "../../../../service/user.service";
import { InterestAreaService } from "../../../../service/interestArea.service";

type DashboardTab = 'users' | 'sensors' | 'areas';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, ScrollingModule, MatTooltipModule, MatRadioModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  activeTab: DashboardTab = 'users';
  isLoading = false;

  users: any[] = [];
  sensors: any[] = [];
  areas: any[] = [];

  isModalOpen = false;
  editingEntity: any = null;
  currentEntityType: string | null = null;
  tempPassword: string = "";

  constructor(
    private userService: UserService,
    private sensorService: SensorService,
    private interestAreaService: InterestAreaService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadDataByTab();
  }

  /**
   * Carica i dati SOLO per la tab attiva.
   * Evita di sovraccaricare il server e la memoria.
   */
  loadDataByTab() {
    this.isLoading = true;

    switch (this.activeTab) {
      case 'users':
        this.fetchUsers();
        break;
      case 'sensors':
        this.fetchSensors();
        break;
      case 'areas':
        this.fetchAreas();
        break;
    }
  }

  private fetchUsers() {
    this.userService.getAll().subscribe({
      next: (res) => {
        this.users = res.map((u: UserDto) => ({ ...u, isEditing: false }));
        this.finalizeLoading();
      },
      error: (err) => this.handleError(err)
    });
  }

  private fetchSensors() {
    this.sensorService.getAllSensorsAdmin().subscribe({
      next: (res) => {
        this.sensors = res.map((s: SensorDto) => ({ ...s, isEditing: false }));
        this.finalizeLoading();
      },
      error: (err) => this.handleError(err)
    });
  }

  private fetchAreas() {
    this.interestAreaService.getAllInterestAreaAdmin().subscribe({
      next: (res) => {
        this.areas = res.map((a: InterestAreaDto) => ({ ...a, isEditing: false }));
        this.finalizeLoading();
      },
      error: (err) => this.handleError(err)
    });
  }

  // --- GESTIONE TAB ---

  switchTab(tab: DashboardTab) {
    if (this.activeTab === tab) return; // Non fare nulla se clicchi la tab già attiva
    this.activeTab = tab;
    this.loadDataByTab(); // Carica i dati della nuova tab
  }

  // --- HELPERS ---

  private finalizeLoading() {
    this.isLoading = false;
    this.cdr.markForCheck();
  }

  private handleError(err: any) {
    console.error("Error loading data:", err);
    this.isLoading = false;
    this.cdr.markForCheck();
  }

  // --- LOGICA EDIT/DELETE/MODAL (Rimasta invariata ma pulita) ---

  startEdit(entity: any) {
    entity.isEditing = true;
    this.cdr.markForCheck();
  }

  async saveEntityInline(entity: any) {
    try {
      console.log("Salvando:", entity);
      entity.isEditing = false;
      this.cdr.markForCheck();
    } catch (error) {
      console.error(error);
    }
  }

  cancelEdit(entity: any) {
    entity.isEditing = false;
    this.loadDataByTab(); // Ricarica per resettare i cambiamenti non salvati
  }

  openEditModal(entity: any, type: DashboardTab) {
    this.editingEntity = { ...entity };
    this.currentEntityType = type;
    this.isModalOpen = true;
  }

  closeModal() {
    this.isModalOpen = false;
    this.editingEntity = null;
    this.currentEntityType = null;
  }

  async saveModalEntity() {
    console.log("Salvando via Modal:", this.editingEntity);
    this.closeModal();
    this.loadDataByTab();
  }

  async deleteEntity(id: string, type: DashboardTab) {
    if (!confirm('Sei sicuro di voler eliminare questo elemento?')) return;
    // Logica delete qui...
    this.loadDataByTab();
  }

  onFileSelected(event: any, field: string) { /* ... */ }
}
