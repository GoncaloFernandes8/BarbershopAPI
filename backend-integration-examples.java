// EXEMPLOS DE INTEGRAÇÃO DAS NOTIFICAÇÕES NOS ENDPOINTS EXISTENTES

// 1. No ClientController - quando criar um novo cliente
@RestController
@RequestMapping("/api/clients")
public class ClientController {
    
    @Autowired
    private ClientService clientService;
    
    @Autowired
    private NotificationService notificationService;
    
    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        Client savedClient = clientService.save(client);
        
        // Criar notificação de novo cliente
        notificationService.notifyNewClient(savedClient.getName());
        
        return ResponseEntity.ok(savedClient);
    }
}

// 2. No AppointmentController - quando criar uma nova marcação
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private ClientService clientService;
    
    @Autowired
    private BarberService barberService;
    
    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {
        Appointment savedAppointment = appointmentService.save(appointment);
        
        // Buscar dados do cliente e barbeiro
        Client client = clientService.findById(appointment.getClientId());
        Barber barber = barberService.findById(appointment.getBarberId());
        
        // Formatar horário
        String time = appointment.getStartsAt().format(DateTimeFormatter.ofPattern("HH:mm"));
        
        // Criar notificação de nova marcação
        notificationService.notifyNewAppointment(
            client.getName(), 
            barber.getName(), 
            time
        );
        
        return ResponseEntity.ok(savedAppointment);
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Appointment> cancelAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.findById(id);
        appointment.setStatus("CANCELLED");
        Appointment savedAppointment = appointmentService.save(appointment);
        
        // Buscar dados do cliente
        Client client = clientService.findById(appointment.getClientId());
        String time = appointment.getStartsAt().format(DateTimeFormatter.ofPattern("HH:mm"));
        
        // Criar notificação de cancelamento
        notificationService.notifyAppointmentCancelled(client.getName(), time);
        
        return ResponseEntity.ok(savedAppointment);
    }
    
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Appointment> confirmAppointment(@PathVariable Long id) {
        Appointment appointment = appointmentService.findById(id);
        appointment.setStatus("CONFIRMED");
        Appointment savedAppointment = appointmentService.save(appointment);
        
        // Buscar dados do cliente
        Client client = clientService.findById(appointment.getClientId());
        String time = appointment.getStartsAt().format(DateTimeFormatter.ofPattern("HH:mm"));
        
        // Criar notificação de confirmação
        notificationService.notifyAppointmentConfirmed(client.getName(), time);
        
        return ResponseEntity.ok(savedAppointment);
    }
}

// 3. No ServiceController - quando criar/atualizar um serviço
@RestController
@RequestMapping("/api/services")
public class ServiceController {
    
    @Autowired
    private ServiceService serviceService;
    
    @Autowired
    private NotificationService notificationService;
    
    @PostMapping
    public ResponseEntity<Service> createService(@RequestBody Service service) {
        Service savedService = serviceService.save(service);
        
        // Criar notificação de novo serviço
        notificationService.notifyNewService(savedService.getName());
        
        return ResponseEntity.ok(savedService);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Service> updateService(@PathVariable Long id, @RequestBody Service service) {
        Service savedService = serviceService.save(service);
        
        // Criar notificação de serviço atualizado
        notificationService.notifyServiceUpdated(savedService.getName());
        
        return ResponseEntity.ok(savedService);
    }
}

// 4. Exemplo de como adicionar notificações em outros eventos
// Por exemplo, quando um barbeiro é adicionado ou quando há problemas no sistema

@RestController
@RequestMapping("/api/barbers")
public class BarberController {
    
    @Autowired
    private BarberService barberService;
    
    @Autowired
    private NotificationService notificationService;
    
    @PostMapping
    public ResponseEntity<Barber> createBarber(@RequestBody Barber barber) {
        Barber savedBarber = barberService.save(barber);
        
        // Criar notificação de novo barbeiro
        notificationService.notifySystemMessage(
            "Novo Barbeiro",
            savedBarber.getName() + " foi adicionado à equipe"
        );
        
        return ResponseEntity.ok(savedBarber);
    }
}

// 5. Exemplo de notificação de sistema (para manutenção, avisos, etc.)
@Service
public class SystemNotificationService {
    
    @Autowired
    private NotificationService notificationService;
    
    public void notifyMaintenance(String message) {
        notificationService.notifySystemMessage("Manutenção", message);
    }
    
    public void notifySystemAlert(String message) {
        notificationService.notifySystemMessage("Alerta do Sistema", message);
    }
    
    public void notifyBackupCompleted() {
        notificationService.notifySystemMessage("Backup", "Backup do sistema concluído com sucesso");
    }
}
