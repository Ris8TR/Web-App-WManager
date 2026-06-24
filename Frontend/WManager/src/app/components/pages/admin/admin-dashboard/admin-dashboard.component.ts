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
import {area} from "@turf/turf";

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

  // Liste con proprietà isEditing aggiunta tramite mapping
  users: any[] = [];
  sensors: any[] = [];
  areas: any[] = [];

  // Per gestione Modal (solo per Areas che richiedono file)
  isModalOpen = false;
  editingEntity: any = null;
  currentEntityType: string | null = null;

  tempPassword: string = "";

  constructor(
    private sensorService: SensorService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData() {
    this.isLoading = true;
    // Esempio di caricamento (Qui dovresti chiamare i tuoi service reali)
    this.sensorService.getAllSensorsAdmin().subscribe({
      next: (response) => {
        this.sensors = response.map((s: SensorDto) => ({ ...s, isEditing: false }));
        // Simuliamo il caricamento degli altri per l'esempio
        this.users = [];
        this.areas = [];
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
        this.cdr.markForCheck();
      }
    });
  }

  switchTab(tab: DashboardTab) {
    this.activeTab = tab;
  }

  // --- LOGICA INLINE EDITING ---

  startEdit(entity: any) {
    entity.isEditing = true;
    this.cdr.markForCheck();
  }

  async saveEntityInline(entity: any) {
    try {
      // Qui inserisci la logica di salvataggio basata sul tipo
      // if (this.activeTab === 'users') ...
      console.log("Salvando in linea:", entity);

      entity.isEditing = false;
      this.cdr.markForCheck();
      // this.loadData(); // Opzionale: ricarica per essere sicuri
    } catch (error) {
      console.error(error);
    }
  }

  cancelEdit(entity: any) {
    entity.isEditing = false;
    this.loadData(); // Ricarica i dati originali dal server
  }

  // --- LOGICA MODAL (Solo per Areas/Files) ---

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
    this.loadData();
  }

  // --- DELETE ---

  async deleteEntity(id: string, type: DashboardTab) {
    if (!confirm('Sei sicuro di voler eliminare questo elemento?')) return;
    // Logica delete...
    this.loadData();
  }

  onFileSelected(event: any, field: string) {
    // Logica file...
  }

  protected readonly area = area;
}
