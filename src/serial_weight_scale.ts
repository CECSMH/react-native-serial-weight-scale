
import type { Device } from "./interfaces/types";
import scale_module from "./interfaces/scale_module";


class SerialWeightScale {
    static listDevices(): Promise<Device[]> { return scale_module.listDevices(); };
    
    constructor() {

    };
};