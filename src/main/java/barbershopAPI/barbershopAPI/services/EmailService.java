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
  <meta name="color-scheme" content="light">
  <meta name="supported-color-schemes" content="light">
</head>
<body style="margin:0;padding:0">
  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#0f1117">
    <tr><td align="center" style="padding:20px">
      <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" style="max-width:600px" bgcolor="#16181d">
        
        <!-- Header -->
        <tr>
          <td align="center" bgcolor="#C3FF5A" style="padding:40px">
            <div style="font-size:26px;font-weight:800;color:#0f1117;font-family:Arial,sans-serif">BARBERSHOP</div>
            <div style="font-size:13px;color:#0f1117;padding-top:6px;font-family:Arial,sans-serif;opacity:0.7">Estilo & Tradição</div>
          </td>
        </tr>
        
        <!-- Alert -->
        <tr>
          <td bgcolor="#16181d" style="padding:40px 40px 0 40px">
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#1f2a1e" style="border:2px solid #C3FF5A;border-radius:8px">
              <tr><td align="center" style="padding:20px;font-size:17px;font-weight:700;color:#C3FF5A;font-family:Arial,sans-serif">A tua marcação é daqui a 1 hora!</td></tr>
            </table>
          </td>
        </tr>
        
        <!-- Título -->
        <tr>
          <td bgcolor="#16181d" style="padding:32px 40px 12px 40px">
            <div style="font-size:24px;font-weight:700;color:#e9eef7;font-family:Arial,sans-serif">Olá, %s</div>
          </td>
        </tr>
        <tr>
          <td bgcolor="#16181d" style="padding:0 40px 32px 40px">
            <div style="font-size:15px;color:#9ca3af;font-family:Arial,sans-serif;line-height:1.6">Este é um lembrete da tua marcação que se aproxima. Preparámos tudo para te receber!</div>
          </td>
        </tr>
        
        <!-- Info Card -->
        <tr>
          <td bgcolor="#16181d" style="padding:0 40px 28px 40px">
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#0f1117" style="border:1px solid #2a3042;border-radius:12px">
              
              <!-- Barbeiro -->
              <tr>
                <td style="padding:18px 20px;border-bottom:1px solid #2a3042">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                    <tr>
                      <td width="100" style="font-size:12px;font-weight:600;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">BARBEIRO</td>
                      <td style="font-size:16px;font-weight:500;color:#e9eef7;font-family:Arial,sans-serif">%s</td>
                    </tr>
                  </table>
                </td>
              </tr>
              
              <!-- Serviço -->
              <tr>
                <td style="padding:18px 20px;border-bottom:1px solid #2a3042">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                    <tr>
                      <td width="100" style="font-size:12px;font-weight:600;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">SERVIÇO</td>
                      <td style="font-size:16px;font-weight:500;color:#e9eef7;font-family:Arial,sans-serif">%s <span style="color:#6b7280">(%d min)</span></td>
                    </tr>
                  </table>
                </td>
              </tr>
              
              <!-- Quando -->
              <tr>
                <td style="padding:18px 20px;border-bottom:1px solid #2a3042">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                    <tr>
                      <td width="100" style="font-size:12px;font-weight:600;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">QUANDO</td>
                      <td style="font-size:16px;font-weight:700;color:#C3FF5A;font-family:Arial,sans-serif">%s às %s</td>
                    </tr>
                  </table>
                </td>
              </tr>
              
              <!-- Termina -->
              <tr>
                <td style="padding:18px 20px;border-bottom:1px solid #2a3042">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                    <tr>
                      <td width="100" style="font-size:12px;font-weight:600;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">TERMINA</td>
                      <td style="font-size:16px;font-weight:500;color:#e9eef7;font-family:Arial,sans-serif">%s</td>
                    </tr>
                  </table>
                </td>
              </tr>
              
              <!-- Preço -->
              <tr>
                <td style="padding:18px 20px;border-bottom:1px solid #2a3042">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                    <tr>
                      <td width="100" style="font-size:12px;font-weight:600;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">PREÇO</td>
                      <td style="font-size:16px;font-weight:700;color:#C3FF5A;font-family:Arial,sans-serif">%s</td>
                    </tr>
                  </table>
                </td>
              </tr>
              
              <!-- Notas -->
              <tr>
                <td style="padding:18px 20px">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                    <tr>
                      <td width="100" style="font-size:12px;font-weight:600;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">NOTAS</td>
                      <td style="font-size:16px;font-weight:500;color:#9ca3af;font-family:Arial,sans-serif">%s</td>
                    </tr>
                  </table>
                </td>
              </tr>
              
            </table>
          </td>
        </tr>
        
        <!-- Dicas -->
        <tr>
          <td bgcolor="#16181d" style="padding:0 40px 32px 40px">
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#1a231a" style="border:1px solid #2f4a2f;border-radius:12px">
              <tr>
                <td style="padding:24px">
                  <div style="font-size:13px;font-weight:700;color:#C3FF5A;text-transform:uppercase;letter-spacing:1px;margin-bottom:16px;font-family:Arial,sans-serif">DICAS PARA A TUA VISITA</div>
                  <div style="font-size:14px;color:#cbd4e6;font-family:Arial,sans-serif;line-height:1.8">
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
          <td bgcolor="#16181d" style="padding:0 40px 32px 40px">
            <div style="font-size:14px;color:#9ca3af;text-align:center;font-family:Arial,sans-serif">
              Precisas de alterar algo? <a href="mailto:geral@barbershop.pt" style="color:#C3FF5A;text-decoration:none;font-weight:600">Contacta-nos</a>
            </div>
          </td>
        </tr>
        
        <!-- Footer -->
        <tr>
          <td bgcolor="#16181d" style="padding:32px 40px;text-align:center;border-top:1px solid #2a3042">
            <div style="font-size:13px;color:#e9eef7;font-weight:700;font-family:Arial,sans-serif;margin-bottom:8px">Barbershop</div>
            <div style="font-size:12px;color:#6b7280;font-family:Arial,sans-serif;line-height:1.8">
              Rua Principal, 123, Lisboa<br>
              (+351) 900 000 000<br><br>
              <a href="#" style="color:#9ca3af;text-decoration:none">Instagram</a> · 
              <a href="#" style="color:#9ca3af;text-decoration:none">Facebook</a> · 
              <a href="#" style="color:#9ca3af;text-decoration:none">Website</a>
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
            serviceName,    // %d
            durationMin,    // %s
            date,           // %s
            startTime,      // %s
            endTime,        // %s
            price,          // %s
            notes           // %s
        );
    }
}
