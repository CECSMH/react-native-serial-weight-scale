import { type Config, type Device, type ScaleResult, type ErrorType, StopBits, Parity, DataBits, BaudRate, Brand } from "./interfaces/types";
import scale_module from "./interfaces/scale_module";

class SerialWeightScale {
    private productId: number;
    private config: Config;

    /**
    * Inicializa uma nova instância de SerialWeightScale.
    *
    * @param productId - Identificador único da balança.
    * @param config - Objeto de configuração da porta serial.
    * @throws {ScaleError} Se a configuração for inválida (por exemplo, marca, taxa de baud ou timeout inválidos).
    */
    constructor(productId: number, config: Config) {
        this.productId = productId;
        this.config = this.validateConfig(config);
    }

    /**
     * Lista todos os dispositivos de balanças seriais disponíveis.
     *
     * @returns 
     * @throws {ScaleError} Se a listagem de dispositivos falhar (tipo: `serial_connection`).
     */
    static async listDevices(): Promise<Device[]> {
        try {
            return await scale_module.listDevices();
        } catch (error: any) {
            throw new ScaleError("serial_connection", `Falha ao listar dispositivos: ${error.message}`);
        }
    }

    /**
     * Estabelece uma conexão com a balança.
     *
     * @returns 
     * @throws {ScaleError} Se a conexão falhar (por exemplo, tipo: `serial_connection`).
     */
    async connect(): Promise<void> {
        try {
            await scale_module.connect(this.productId, this.config);
        } catch (error) {
            throw this.parseNativeError(error);
        }
    }

    /**
     * Lê o peso atual da balança.
     *
     * @returns
     * @throws {ScaleError} Se a leitura falhar (por exemplo, tipo: `serial_connection`, `invalid_response`).
     */
    async readWeight(): Promise<number> {
        try {
            const result: ScaleResult = await scale_module.readWeight(this.productId);
            if (result.error) throw this.parseNativeError(result.error);
            return result.weight ?? 0;
        } catch (error) {
            throw this.parseNativeError(error);
        }
    }

    /**
     * Desconecta a balança.
     *
     * @returns 
     * @throws {ScaleError} Se a desconexão falhar (tipo: `serial_connection`).
     */
    async disconnect(): Promise<void> {
        try {
            await scale_module.disconnect(this.productId);
        } catch (error) {
            throw this.parseNativeError(error);
        }
    }

    /**
     * Monitora alterações de peso e invoca o callback fornecido com cada nova leitura de peso.
     *
     * @param callback - Função chamada com o peso mais recente (ou 0 se nenhum peso estiver disponível).
     * @returns Uma função que, quando chamada, interrompe o monitoramento.
     * @throws {ScaleError} Se o monitoramento falhar (por exemplo, tipo: `serial_connection`).
     */
    monitorWeight(callback: (weight: number) => void): () => void {
        try {
            const listener = scale_module.monitorWeight(this.productId, (result: ScaleResult) => {
                if (result.error) throw this.parseNativeError(result.error);
                callback(result.weight ?? 0);
            });
            return listener;
        } catch (error: any) {
            throw this.parseNativeError(error);
        }
    }

    /**
     * Converte erros do módulo nativo em instâncias de ScaleError.
     *
     * @param error - O objeto de erro do módulo nativo.
     * @returns Um ScaleError com tipo e mensagem apropriados.
     * @private
     */
    private parseNativeError(error: any): ScaleError {
        if (error?.code) {
            return new ScaleError(error.code, error.message);
        }
        return new ScaleError("serial_connection", `Erro inesperado: ${error.message}`);
    }

    /**
     * Valida o objeto de configuração, aplicando padrões e garantindo valores válidos.
     *
     * @param config - O objeto de configuração a ser validado.
     * @returns O objeto de configuração validado.
     * @throws {ScaleError} Se algum campo de configuração for inválido.
     * @private
     */
    private validateConfig(config: Config): Config {
        if (!Object.values(Brand).includes(config.brand)) {
            throw new ScaleError("invalid_scale_id", `Marca inválida: ${config.brand}. Deve ser uma de ${Object.values(Brand).join(", ")}`);
        }
        if (!Object.values(BaudRate).includes(config.baudRate)) {
            throw new ScaleError("calibration_error", `Taxa de baud inválida: ${config.baudRate}. Deve ser uma de ${Object.values(BaudRate).join(", ")}`);
        }
        if (!Object.values(DataBits).includes(config.dataBits)) {
            throw new ScaleError("invalid_response", `Bits de dados inválidos: ${config.dataBits}. Deve ser um de ${Object.values(DataBits).join(", ")}`);
        }

        config.parity = config.parity || Parity.None;
        if (!Object.values(Parity).includes(config.parity)) {
            throw new ScaleError("calibration_error", `Paridade inválida: ${config.parity}. Deve ser uma de ${Object.values(Parity).join(", ")}`);
        }

        config.stopBits = config.stopBits || StopBits.One;
        if (!Object.values(StopBits).includes(config.stopBits)) {
            throw new ScaleError("invalid_response", `Bits de parada inválidos: ${config.stopBits}. Deve ser um de ${Object.values(StopBits).join(", ")}`);
        }

        config.timeout = config.timeout ?? 600;
        if (config.timeout < 100 || config.timeout > 5000) {
            throw new ScaleError("timeout", `O timeout deve estar entre 100ms e 5000ms, mas foi recebido ${config.timeout}ms.`);
        }

        config.retries = config.retries ?? 4;
        if (config.retries && config.retries < 4) {
            throw new ScaleError("serial_connection", `As tentativas devem ser 4 ou mais, mas foi recebido ${config.retries}.`);
        }
        return config;
    }

    /**
     * Desconecta todas as balanças conectadas.
     *
     * @throws {ScaleError} Se a desconexão falhar (tipo: `serial_connection`).
     */
    static async disconnectAll(): Promise<void> {
        try {
            await scale_module.disconnectAll();
        } catch (error: any) {
            throw new ScaleError("serial_connection", `Falha ao desconectar todas as balanças: ${error.message}`);
        }
    }
};

class ScaleError extends Error {
    type: ErrorType;

    constructor(type: ErrorType, message: string) {
        super(message);
        this.type = type;
    };
};

export default SerialWeightScale;