export type ErrorType =
    | 'unstable_weight'
    | 'negative_weight'
    | 'timeout'
    | 'overload'
    | 'zero_capture'
    | 'calibration_error'
    | 'invalid_response'
    | 'serial_connection'
    | 'invalid_scale_id';

export interface Device {
    name: string;
    vendorId: number;
    productId: number;
    port: string;
    hasPermission: boolean;
}

export enum StopBits {
    One = 1,
    OnePointFive = 3,
    Two = 2
}

export enum BaudRate {
    Low = 2400,
    Medium = 4800,
    Standard = 9600,
    High = 115200
}

export enum DataBits {
    Five = 5,
    Six = 6,
    Seven = 7,
    Eight = 8
}

export enum Parity {
    None = "none",
    Even = "even",
    Odd = "odd"
}

export enum Brand {
    Toledo = "toledo",
    Filizola = "filizola",
    Urano = "urano",
    Micheletti = "micheletti"
}

export interface Config {
    brand: Brand; 
    baudRate: BaudRate;
    dataBits: DataBits;
    parity: Parity;
    stopBits: StopBits;
    timeout?: number;
    retries?: number;
}
export interface ScaleError {
    code: number;
    message: string;
}

export interface ScaleResult {
    weight?: number;
    error?: ScaleError;
}