import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserDto } from "../../../../model/userDto";
import { InterestAreaDto } from "../../../../model/interestAreaDto";
import { SensorDto } from "../../../../model/sensorDto";
import { SensorService } from "../../../../service/sensor.service";
import { UserService } from "../../../../service/user.service";
import { InterestAreaService } from "../../../../service/interestArea.service";

// Angular Material & CDK
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatRadioModule } from '@angular/material/radio';
import { ScrollingModule } from '@angular/cdk/scrolling';

type DashboardTab = 'users' | 'sensors' | 'areas';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ScrollingModule,
    MatTooltipModule,
    MatRadioModule
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  // --- STATE MANAGEMENT ---
  activeTab: DashboardTab = 'users';
  isLoading = false;

  // Dati originali (Source of truth)
  users: any[] = [];
  sensors: any[] = [];
  areas: any[] = [];

  // Dati filtrati (Quelli che vengono visualizzati nel template con *cdkVirtualFor)
  // ATTENZIONE: Nel template devi usare filteredUsers, filteredSensors, filteredAreas
  filteredUsers: any[] = [];
  filteredSensors: any[] = [];
  filteredAreas: any[] = [];

  // Oggetto per gestire i valori dei filtri
  filters = {
    users: {
      search: ''
    },
    sensors: {
      search: '',
      isPublic: null as boolean | null
    },
    areas: {
      search: '',
      isPublic: null as boolean | null
    }
  };

  // --- MODAL & EDITING STATE ---
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
   * Carica i dati in base alla tab attiva e resetta i filtri.
   */
  loadDataByTab() {
    this.isLoading = true;
    // Reset filtri quando si cambia tab per pulizia UX
    this.resetFilters();

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

  // --- DATA FETCHING ---

  private fetchUsers() {
    this.userService.getAll().subscribe({
      next: (res) => {
        this.users = res.map((u: UserDto) => ({ ...u, isEditing: false }));
        this.applyFilters(); // Popola filteredUsers
        console.log(res);
        this.finalizeLoading();
      },
      error: (err) => this.handleError(err)
    });
  }

  private fetchSensors() {
    this.sensorService.getAllSensorsAdmin().subscribe({
      next: (res) => {
        this.sensors = res.map((s: SensorDto) => ({ ...s, isEditing: false }));
        console.log(this.sensors);
        this.applyFilters(); // Popola filteredSensors
        this.finalizeLoading();
      },
      error: (err) => this.handleError(err)
    });
  }

  private fetchAreas() {
    this.interestAreaService.getAllInterestAreaAdmin().subscribe({
      next: (res) => {
        this.areas = res.map((a: InterestAreaDto) => ({ ...a, isEditing: false }));
        this.applyFilters(); // Popola filteredAreas
        this.finalizeLoading();
      },
      error: (err) => this.handleError(err)
    });
  }

  // --- TAB & FILTER LOGIC ---

  switchTab(tab: DashboardTab) {
    if (this.activeTab === tab) return;
    this.activeTab = tab;
    this.loadDataByTab();
  }

  /**
   * Esegue il filtraggio lato client sugli array originali
   * e aggiorna gli array filtrati.
   */

  applyFilters() {
    // 1. Filtro Users
    const userSearch = this.filters.users.search.toLowerCase();
    this.filteredUsers = this.users.filter(u =>
      u.firstName.toLowerCase().includes(userSearch) ||
      u.lastName.toLowerCase().includes(userSearch) ||
      u.email.toLowerCase().includes(userSearch)
    );

    // 2. Filtro Sensors
    const sensorSearch = this.filters.sensors.search.toLowerCase();
    this.filteredSensors = this.sensors.filter(s => {
      const matchSearch =
        s.companyName.toLowerCase().includes(sensorSearch) ||
        s.id.toLowerCase().includes(sensorSearch) ||
        String(s.userId).toLowerCase().includes(sensorSearch) ||
        String(s.interestAreaID).toLowerCase().includes(sensorSearch);
      const matchVis = this.filters.sensors.isPublic === null || s.isPublic === this.filters.sensors.isPublic;
      return matchSearch && matchVis;
    });

    // 3. Filtro Areas
    const areaSearch = this.filters.areas.search.toLowerCase();
    this.filteredAreas = this.areas.filter(a => {
      const matchSearch = a.name.toLowerCase().includes(areaSearch) ||
        a.description.toLowerCase().includes(areaSearch);
      const matchVis = this.filters.areas.isPublic === null || a.isPublic === this.filters.areas.isPublic;
      return matchSearch && matchVis;
    });

    this.cdr.markForCheck();
  }

  resetFilters() {
    this.filters = {
      users: { search: '' },
      sensors: { search: '',  isPublic: null },
      areas: { search: '', isPublic: null }
    };
    this.applyFilters();
  }

  // --- EDITING & CRUD LOGIC ---

  startEdit(entity: any) {
    entity.isEditing = true;
    this.cdr.markForCheck();
  }

  async saveEntityInline(entity: any) {
    try {
      console.log("Salvando entità:", entity);
      // Qui andrebbe la chiamata al servizio: this.userService.update(entity)...
      entity.isEditing = false;
      this.cdr.markForCheck();
    } catch (error) {
      console.error("Errore durante il salvataggio:", error);
    }
  }

  cancelEdit(entity: any) {
    entity.isEditing = false;
    this.loadDataByTab(); // Ricarica per annullare le modifiche non salvate
  }

  openEditModal(entity: any, type: DashboardTab) {
    this.editingEntity = { ...entity };
    this.currentEntityType = type;
    this.isModalOpen = true;
    this.cdr.detectChanges();
  }


  trackById(index: number, item: any): any {
    return item.id;
  }

  closeModal() {
    this.isModalOpen = false;
    this.editingEntity = null;
    this.currentEntityType = null;
  }

  async saveModalEntity() {
    try {
      console.log("Salvando tramite Modal:", this.editingEntity);
      // Qui andrebbe la chiamata al servizio per l'area
      this.closeModal();
      this.loadDataByTab();
    } catch (error) {
      console.error("Errore salvataggio modal:", error);
    }
  }

  async deleteEntity(id: string, type: DashboardTab) {
    if (!confirm(`Sei sicuro di voler eliminare questo elemento (${type})?`)) return;

    try {
      console.log(`Eliminando ${type} con ID: ${id}`);
      // Qui chiameresti il servizio: this.userService.delete(id)...
      this.loadDataByTab();
    } catch (error) {
      console.error("Errore durante l'eliminazione:", error);
    }
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

  onFileSelected(event: any, field: string) {
    const file = event.target.files[0];
    if (file) {
      console.log(`File selezionato per ${field}:`, file.name);
      // Logica di caricamento file...
    }
  }
}
