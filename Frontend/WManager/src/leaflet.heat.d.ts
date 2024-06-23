import * as L from 'leaflet';
import {Layer} from "leaflet";

declare module 'leaflet' {
  function heatLayer(latlngs: (number | undefined)[][], options?: any): Layer;
}
