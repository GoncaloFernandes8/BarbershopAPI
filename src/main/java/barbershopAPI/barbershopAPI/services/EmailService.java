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
    
    private static final String FROM_EMAIL = System.getenv().getOrDefault("MAIL_FROM", "no-reply@barbershop.pt");
    private static final String FROM_NAME = System.getenv().getOrDefault("MAIL_FROM_NAME", "Barbershop");
    
    public void sendAppointmentReminder(Appointment appointment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(FROM_EMAIL, FROM_NAME);
            helper.setTo(appointment.getClient().getEmail());
            helper.setSubject("Lembrete: A tua marcação é daqui a 1 hora");
            helper.setText(buildReminderEmailHtml(appointment), true);
            
            mailSender.send(message);
            log.info("Email de lembrete enviado para {}", appointment.getClient().getEmail());
                     
        } catch (MessagingException e) {
            log.error("Erro ao enviar email de lembrete: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao enviar email: {}", e.getMessage(), e);
        }
    }
    
    private String buildReminderEmailHtml(Appointment appointment) {
        String clientName = appointment.getClient().getName();
        String barberName = appointment.getBarber().getName();
        String serviceName = appointment.getService().getName();
        int durationMin = appointment.getService().getDurationMin();
        String date = appointment.getStartsAt().format(DATE_FORMATTER);
        String startTime = appointment.getStartsAt().format(TIME_FORMATTER);
        String endTime = appointment.getEndsAt().format(TIME_FORMATTER);
        String price = String.format("%.2f€", appointment.getService().getPriceCents() / 100.0);
        String notes = appointment.getNotes() != null && !appointment.getNotes().isBlank() 
                      ? appointment.getNotes() 
                      : "Sem observações";
        
        return String.format("""
<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
</head>
<body style="margin:0;padding:0;background-color:#f5f5f5">
  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#f5f5f5">
    <tr><td align="center" style="padding:40px 20px">
      <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" style="max-width:600px;border-radius:12px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08)" bgcolor="#ffffff">
        
        <!-- Header -->
        <tr>
          <td align="center" bgcolor="#C3FF5A" style="padding:48px 40px">
            <div style="font-size:32px;font-weight:800;color:#0f1117;font-family:Arial,sans-serif;letter-spacing:-0.5px">BARBERSHOP</div>
            <div style="font-size:14px;color:#0f1117;padding-top:8px;font-family:Arial,sans-serif;opacity:0.7;font-weight:500">Estilo & Tradição</div>
          </td>
        </tr>
        
        <!-- Alert -->
        <tr>
          <td bgcolor="#ffffff" style="padding:32px 40px 0 40px">
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#fffbeb" style="border-left:4px solid #C3FF5A;border-radius:12px;overflow:hidden">
              <tr><td align="center" style="padding:20px;font-size:18px;font-weight:700;color:#0f1117;font-family:Arial,sans-serif">A tua marcação é daqui a 1 hora!</td></tr>
            </table>
          </td>
        </tr>
        
        <!-- Saudação -->
        <tr>
          <td bgcolor="#ffffff" style="padding:32px 40px 16px 40px">
            <div style="font-size:24px;font-weight:700;color:#111111;font-family:Arial,sans-serif">Olá, %s</div>
          </td>
        </tr>
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 32px 40px">
            <div style="font-size:16px;color:#666666;font-family:Arial,sans-serif;line-height:1.6">Este é um lembrete da tua marcação que se aproxima. Preparámos tudo para te receber!</div>
          </td>
        </tr>
        
        <!-- Info Card -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 32px 40px">
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#f9fafb" style="border:1px solid #e5e7eb;border-radius:12px;overflow:hidden">
              
              <!-- Barbeiro -->
              <tr>
                <td style="padding:16px 20px;border-bottom:1px solid #e5e7eb">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                    <tr>
                      <td width="110" style="font-size:12px;font-weight:700;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">Barbeiro</td>
                      <td style="font-size:16px;font-weight:600;color:#111111;font-family:Arial,sans-serif">%s</td>
                    </tr>
                  </table>
                </td>
              </tr>
              
              <!-- Serviço -->
              <tr>
                <td style="padding:16px 20px;border-bottom:1px solid #e5e7eb">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                    <tr>
                      <td width="110" style="font-size:12px;font-weight:700;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">Serviço</td>
                      <td style="font-size:16px;font-weight:600;color:#111111;font-family:Arial,sans-serif">%s <span style="color:#6b7280;font-weight:400">(%d min)</span></td>
                    </tr>
                  </table>
                </td>
              </tr>
              
              <!-- Quando -->
              <tr>
                <td style="padding:16px 20px;border-bottom:1px solid #e5e7eb">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                    <tr>
                      <td width="110" style="font-size:12px;font-weight:700;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">Quando</td>
                      <td style="font-size:16px;font-weight:700;color:#0f1117;font-family:Arial,sans-serif">%s às %s</td>
                    </tr>
                  </table>
                </td>
              </tr>
              
              <!-- Termina -->
              <tr>
                <td style="padding:16px 20px;border-bottom:1px solid #e5e7eb">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                    <tr>
                      <td width="110" style="font-size:12px;font-weight:700;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">Termina</td>
                      <td style="font-size:16px;font-weight:600;color:#111111;font-family:Arial,sans-serif">%s</td>
                    </tr>
                  </table>
                </td>
              </tr>
              
              <!-- Preço -->
              <tr>
                <td style="padding:16px 20px;border-bottom:1px solid #e5e7eb;background-color:#f0fdf4">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                    <tr>
                      <td width="110" style="font-size:12px;font-weight:700;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">Preço</td>
                      <td style="font-size:18px;font-weight:800;color:#0f1117;font-family:Arial,sans-serif">%s</td>
                    </tr>
                  </table>
                </td>
              </tr>
              
              <!-- Notas -->
              <tr>
                <td style="padding:16px 20px">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                    <tr>
                      <td width="110" style="font-size:12px;font-weight:700;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px;vertical-align:top">Notas</td>
                      <td style="font-size:14px;color:#666666;font-family:Arial,sans-serif;line-height:1.5">%s</td>
                    </tr>
                  </table>
                </td>
              </tr>
              
            </table>
          </td>
        </tr>
        
        <!-- Dicas -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 32px 40px">
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#f0fdf4" style="border:2px solid #C3FF5A;border-radius:12px;overflow:hidden">
              <tr>
                <td style="padding:24px">
                  <div style="font-size:13px;font-weight:700;color:#0f1117;text-transform:uppercase;letter-spacing:1px;margin-bottom:16px;font-family:Arial,sans-serif">⚡ Dicas para a tua visita</div>
                  <div style="font-size:14px;color:#374151;font-family:Arial,sans-serif;line-height:1.8">
                    • Chega com 5 minutos de antecedência<br>
                    • Se precisares de cancelar, avisa com antecedência<br>
                    • Traz uma foto de referência se tiveres<br>
                    • Estacionamento disponível na rua
                  </div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        
        <!-- Contacto -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 40px 40px;text-align:center">
            <div style="font-size:14px;color:#6b7280;font-family:Arial,sans-serif">
              Precisas de alterar algo? <a href="mailto:geral@barbershop.pt" style="color:#0f1117;text-decoration:none;font-weight:700;border-bottom:2px solid #C3FF5A">Contacta-nos</a>
            </div>
          </td>
        </tr>
        
        <!-- Footer -->
        <tr>
          <td bgcolor="#111111" style="padding:32px 40px;text-align:center">
            <div style="font-size:14px;color:#ffffff;font-weight:700;font-family:Arial,sans-serif;margin-bottom:8px">Barbershop</div>
            <div style="font-size:12px;color:#9ca3af;font-family:Arial,sans-serif;line-height:1.8">
              Rua Principal, 123, Lisboa<br>
              (+351) 900 000 000<br><br>
              <a href="#" style="color:#C3FF5A;text-decoration:none">Instagram</a> · 
              <a href="#" style="color:#C3FF5A;text-decoration:none">Facebook</a> · 
              <a href="#" style="color:#C3FF5A;text-decoration:none">Website</a>
            </div>
          </td>
        </tr>
        
      </table>
    </td></tr>
  </table>
</body>
</html>
            """, 
            clientName,     // %s
            barberName,     // %s
            serviceName,    // %s
            durationMin,    // %d
            date,           // %s
            startTime,      // %s
            endTime,        // %s
            price,          // %s
            notes           // %s
        );
    }
}
