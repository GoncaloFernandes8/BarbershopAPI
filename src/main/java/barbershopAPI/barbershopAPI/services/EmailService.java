package barbershopAPI.barbershopAPI.services;

import barbershopAPI.barbershopAPI.entities.Appointment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-PT"));
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("pt-PT"));
    
    /**
     * Envia email de lembrete de marcação
     */
    public void sendAppointmentReminder(Appointment appointment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(appointment.getClient().getEmail());
            helper.setSubject("✂️ Lembrete: A sua marcação é daqui a 1 hora!");
            helper.setText(buildReminderEmailHtml(appointment), true);
            
            mailSender.send(message);
            log.info("Email de lembrete enviado para {} (Marcação: {})", 
                     appointment.getClient().getEmail(), 
                     appointment.getId());
                     
        } catch (MessagingException e) {
            log.error("Erro ao enviar email de lembrete para {}: {}", 
                      appointment.getClient().getEmail(), 
                      e.getMessage());
        }
    }
    
    /**
     * Constrói o HTML do email de lembrete
     */
    private String buildReminderEmailHtml(Appointment appointment) {
        String clientName = appointment.getClient().getName();
        String barberName = appointment.getBarber().getName();
        String serviceName = appointment.getService().getName();
        String date = appointment.getStartsAt().format(DATE_FORMATTER);
        String startTime = appointment.getStartsAt().format(TIME_FORMATTER);
        String endTime = appointment.getEndsAt().format(TIME_FORMATTER);
        String duration = appointment.getService().getDurationMin() + " minutos";
        String price = String.format("%.2f€", appointment.getService().getPriceCents() / 100.0);
        String notes = appointment.getNotes() != null && !appointment.getNotes().isBlank() 
                      ? appointment.getNotes() 
                      : "Nenhuma observação";
        
        return String.format("""
            <!DOCTYPE html>
            <html lang="pt-PT">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Lembrete de Marcação</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4;">
                <table cellpadding="0" cellspacing="0" width="100%%" style="background-color: #f4f4f4; padding: 40px 0;">
                    <tr>
                        <td align="center">
                            <!-- Container Principal -->
                            <table cellpadding="0" cellspacing="0" width="600" style="background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); overflow: hidden;">
                                
                                <!-- Header com degradê -->
                                <tr>
                                    <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                                        <h1 style="margin: 0; color: #ffffff; font-size: 32px; font-weight: 700;">✂️ Barbershop</h1>
                                        <p style="margin: 10px 0 0 0; color: #f0f0f0; font-size: 16px;">Lembrete da Sua Marcação</p>
                                    </td>
                                </tr>
                                
                                <!-- Alerta de 1 hora -->
                                <tr>
                                    <td style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 20px 30px;">
                                        <p style="margin: 0; color: #856404; font-size: 18px; font-weight: 600;">
                                            ⏰ A sua marcação é daqui a 1 hora!
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Saudação -->
                                <tr>
                                    <td style="padding: 30px 30px 20px 30px;">
                                        <p style="margin: 0; color: #333333; font-size: 18px; line-height: 1.6;">
                                            Olá <strong>%s</strong>,
                                        </p>
                                        <p style="margin: 15px 0 0 0; color: #666666; font-size: 16px; line-height: 1.6;">
                                            Este é um lembrete amigável da sua marcação que se aproxima. Preparámos tudo para o receber!
                                        </p>
                                    </td>
                                </tr>
                                
                                <!-- Detalhes da Marcação -->
                                <tr>
                                    <td style="padding: 0 30px 30px 30px;">
                                        <table cellpadding="0" cellspacing="0" width="100%%" style="background-color: #f8f9fa; border-radius: 8px; padding: 25px;">
                                            <tr>
                                                <td colspan="2" style="padding-bottom: 20px;">
                                                    <h2 style="margin: 0; color: #667eea; font-size: 22px; font-weight: 700;">📋 Detalhes da Marcação</h2>
                                                </td>
                                            </tr>
                                            
                                            <!-- Barbeiro -->
                                            <tr>
                                                <td style="padding: 10px 0; color: #666666; font-size: 14px; width: 140px;">
                                                    👤 <strong>Barbeiro:</strong>
                                                </td>
                                                <td style="padding: 10px 0; color: #333333; font-size: 16px; font-weight: 600;">
                                                    %s
                                                </td>
                                            </tr>
                                            
                                            <!-- Serviço -->
                                            <tr>
                                                <td style="padding: 10px 0; color: #666666; font-size: 14px;">
                                                    ✂️ <strong>Serviço:</strong>
                                                </td>
                                                <td style="padding: 10px 0; color: #333333; font-size: 16px; font-weight: 600;">
                                                    %s
                                                </td>
                                            </tr>
                                            
                                            <!-- Data -->
                                            <tr>
                                                <td style="padding: 10px 0; color: #666666; font-size: 14px;">
                                                    📅 <strong>Data:</strong>
                                                </td>
                                                <td style="padding: 10px 0; color: #333333; font-size: 16px; font-weight: 600;">
                                                    %s
                                                </td>
                                            </tr>
                                            
                                            <!-- Horário -->
                                            <tr>
                                                <td style="padding: 10px 0; color: #666666; font-size: 14px;">
                                                    🕐 <strong>Horário:</strong>
                                                </td>
                                                <td style="padding: 10px 0; color: #333333; font-size: 16px; font-weight: 600;">
                                                    %s - %s
                                                </td>
                                            </tr>
                                            
                                            <!-- Duração -->
                                            <tr>
                                                <td style="padding: 10px 0; color: #666666; font-size: 14px;">
                                                    ⏱️ <strong>Duração:</strong>
                                                </td>
                                                <td style="padding: 10px 0; color: #333333; font-size: 16px; font-weight: 600;">
                                                    %s
                                                </td>
                                            </tr>
                                            
                                            <!-- Preço -->
                                            <tr>
                                                <td style="padding: 10px 0; color: #666666; font-size: 14px;">
                                                    💰 <strong>Preço:</strong>
                                                </td>
                                                <td style="padding: 10px 0; color: #667eea; font-size: 18px; font-weight: 700;">
                                                    %s
                                                </td>
                                            </tr>
                                            
                                            <!-- Observações -->
                                            <tr>
                                                <td style="padding: 10px 0; color: #666666; font-size: 14px; vertical-align: top;">
                                                    📝 <strong>Observações:</strong>
                                                </td>
                                                <td style="padding: 10px 0; color: #666666; font-size: 14px; line-height: 1.5;">
                                                    %s
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                
                                <!-- Dicas -->
                                <tr>
                                    <td style="padding: 0 30px 30px 30px;">
                                        <table cellpadding="0" cellspacing="0" width="100%%" style="background-color: #e8f4fd; border-radius: 8px; padding: 20px;">
                                            <tr>
                                                <td>
                                                    <h3 style="margin: 0 0 15px 0; color: #0277bd; font-size: 18px; font-weight: 700;">💡 Dicas para a sua visita:</h3>
                                                    <ul style="margin: 0; padding-left: 20px; color: #555555; font-size: 14px; line-height: 1.8;">
                                                        <li>Chegue com 5 minutos de antecedência</li>
                                                        <li>Se precisar cancelar, avise com antecedência</li>
                                                        <li>Traga uma foto de referência se tiver</li>
                                                        <li>Estacionamento disponível na rua</li>
                                                    </ul>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="background-color: #2c3e50; padding: 30px; text-align: center;">
                                        <p style="margin: 0 0 10px 0; color: #ecf0f1; font-size: 14px;">
                                            Até já! Esperamos por si! 👋
                                        </p>
                                        <p style="margin: 0; color: #95a5a6; font-size: 12px;">
                                            Este é um email automático. Por favor não responda.
                                        </p>
                                        <p style="margin: 15px 0 0 0; color: #95a5a6; font-size: 12px;">
                                            © 2025 Barbershop. Todos os direitos reservados.
                                        </p>
                                    </td>
                                </tr>
                                
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """, 
            clientName,     // %s 1
            barberName,     // %s 2
            serviceName,    // %s 3
            date,           // %s 4
            startTime,      // %s 5
            endTime,        // %s 6
            duration,       // %s 7
            price,          // %s 8
            notes           // %s 9
        );
    }
}

