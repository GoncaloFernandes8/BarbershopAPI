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
    
    // Email remetente - usar variável de ambiente ou fallback
    private static final String FROM_EMAIL = System.getenv().getOrDefault("MAIL_FROM", "no-reply@barbershop.pt");
    private static final String FROM_NAME = System.getenv().getOrDefault("MAIL_FROM_NAME", "Barbershop");
    
    /**
     * Envia email de lembrete de marcação
     */
    public void sendAppointmentReminder(Appointment appointment) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // ✅ CORRIGIDO: Definir remetente
            helper.setFrom(FROM_EMAIL, FROM_NAME);
            helper.setTo(appointment.getClient().getEmail());
            helper.setSubject("Lembrete: A tua marcação é daqui a 1 hora");
            helper.setText(buildReminderEmailHtml(appointment), true);
            
            mailSender.send(message);
            log.info("✅ Email de lembrete enviado para {} (Marcação: {})", 
                     appointment.getClient().getEmail(), 
                     appointment.getId());
                     
        } catch (MessagingException e) {
            log.error("❌ Erro ao enviar email de lembrete para {}: {}", 
                      appointment.getClient().getEmail(), 
                      e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Erro inesperado ao enviar email: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Constrói o HTML do email de lembrete - Estilo padronizado
     */
    private String buildReminderEmailHtml(Appointment appointment) {
        String clientName = appointment.getClient().getName();
        String barberName = appointment.getBarber().getName();
        String serviceName = appointment.getService().getName();
        String date = appointment.getStartsAt().format(DATE_FORMATTER);
        String startTime = appointment.getStartsAt().format(TIME_FORMATTER);
        String endTime = appointment.getEndsAt().format(TIME_FORMATTER);
        String durationText = appointment.getService().getDurationMin() + " min";
        String price = String.format("%.2f€", appointment.getService().getPriceCents() / 100.0);
        String notes = appointment.getNotes() != null && !appointment.getNotes().isBlank() 
                      ? appointment.getNotes() 
                      : "Sem observações";
        
        return String.format("""
    <!doctype html>
    <html lang="pt">
    <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>Lembrete de Marcação</title>
      <style>
        body{margin:0;padding:0;background:#0f1117;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif}
        .wrapper{width:100%%;background:#0f1117;padding:40px 20px}
        .container{max-width:600px;margin:0 auto;background:#16181d;border:1px solid #2a3042;border-radius:16px;overflow:hidden}
        .header{background:#C3FF5A;padding:40px;text-align:center}
        .logo-icon{width:48px;height:48px;margin:0 auto 12px;color:#0f1117}
        .brand{font-size:26px;font-weight:800;color:#0f1117;margin:0;letter-spacing:-0.5px}
        .tagline{font-size:13px;color:rgba(15,17,23,0.6);margin:6px 0 0;font-weight:500}
        .content{padding:40px}
        .alert{background:rgba(195,255,90,0.15);border:1px solid rgba(195,255,90,0.4);border-left:4px solid #C3FF5A;padding:20px;border-radius:10px;margin:0 0 32px;display:flex;align-items:center;gap:14px}
        .alert-icon{color:#C3FF5A;flex-shrink:0}
        .alert-text{color:#e9eef7;font-size:17px;font-weight:700;margin:0;flex:1}
        .title{font-size:24px;font-weight:700;color:#e9eef7;margin:0 0 12px;line-height:1.3}
        .subtitle{color:#9ca3af;font-size:15px;line-height:1.6;margin:0 0 32px}
        .info-card{background:rgba(0,0,0,0.3);border:1px solid #2a3042;border-radius:12px;padding:0;margin:0 0 28px;overflow:hidden}
        .info-row{display:flex;align-items:center;padding:18px 20px;border-bottom:1px solid #2a3042}
        .info-row:last-child{border-bottom:none}
        .info-icon{color:#C3FF5A;margin-right:14px;display:flex;align-items:center;flex-shrink:0}
        .info-label{color:#6b7280;font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:0.8px;min-width:100px;flex-shrink:0}
        .info-value{color:#e9eef7;font-size:16px;font-weight:500;flex:1}
        .highlight{color:#C3FF5A;font-weight:700}
        .tips-card{background:rgba(195,255,90,0.08);border:1px solid rgba(195,255,90,0.2);border-radius:12px;padding:24px;margin:0 0 32px}
        .tips-title{color:#C3FF5A;font-size:13px;font-weight:700;text-transform:uppercase;margin:0 0 16px;letter-spacing:1px;display:flex;align-items:center;gap:8px}
        .tips-icon{color:#C3FF5A}
        .tips-list{margin:0;padding-left:20px;color:#cbd4e6;font-size:14px;line-height:1.8}
        .tips-list li{margin:8px 0}
        .divider{height:1px;background:linear-gradient(90deg,transparent,rgba(195,255,90,0.2),transparent);margin:32px 0}
        .footer{padding:32px 40px;text-align:center;color:#6b7280;font-size:12px;line-height:1.8;border-top:1px solid #2a3042}
        .footer-link{color:#9ca3af;text-decoration:none;transition:color 0.2s}
        .footer-link:hover{color:#C3FF5A}
      </style>
    </head>
    <body>
      <div class="wrapper">
        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
          <tr><td align="center">
            <div class="container">
              <div class="header">
                <svg class="logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="6" cy="6" r="3"/><circle cx="6" cy="18" r="3"/>
                  <path d="M20 4L8.12 15.88M14.47 14.48L20 20M8.12 8.12L12 12"/>
                </svg>
                <h1 class="brand">BARBERSHOP</h1>
                <p class="tagline">Estilo & Tradição</p>
              </div>
              
              <div class="content">
                <div class="alert">
                  <svg class="alert-icon" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
                  </svg>
                  <p class="alert-text">A tua marcação é daqui a 1 hora!</p>
                </div>
                
                <h2 class="title">Olá, %s</h2>
                <p class="subtitle">Este é um lembrete da tua marcação que se aproxima. Preparámos tudo para te receber!</p>
                
                <div class="info-card">
                  <div class="info-row">
                    <div class="info-icon">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                      </svg>
                    </div>
                    <div class="info-label">Barbeiro</div>
                    <div class="info-value">%s</div>
                  </div>
                  <div class="info-row">
                    <div class="info-icon">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="6" cy="6" r="3"/><circle cx="6" cy="18" r="3"/>
                        <path d="M20 4L8.12 15.88M14.47 14.48L20 20M8.12 8.12L12 12"/>
                      </svg>
                    </div>
                    <div class="info-label">Serviço</div>
                    <div class="info-value">%s <span style="color:#6b7280">(%s)</span></div>
                  </div>
                  <div class="info-row">
                    <div class="info-icon">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/>
                      </svg>
                    </div>
                    <div class="info-label">Quando</div>
                    <div class="info-value highlight">%s às %s</div>
                  </div>
                  <div class="info-row">
                    <div class="info-icon">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"/><path d="M12 2v12l4.5 4.5"/>
                      </svg>
                    </div>
                    <div class="info-label">Termina</div>
                    <div class="info-value">%s</div>
                  </div>
                  <div class="info-row">
                    <div class="info-icon">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
                      </svg>
                    </div>
                    <div class="info-label">Preço</div>
                    <div class="info-value highlight">%s</div>
                  </div>
                  <div class="info-row">
                    <div class="info-icon">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                        <path d="M14 2v6h6M16 13H8M16 17H8M10 9H8"/>
                      </svg>
                    </div>
                    <div class="info-label">Notas</div>
                    <div class="info-value" style="color:#9ca3af">%s</div>
                  </div>
                </div>
                
                <div class="tips-card">
                  <div class="tips-title">
                    <svg class="tips-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/>
                    </svg>
                    Dicas para a tua visita
                  </div>
                  <ul class="tips-list">
                    <li>Chega com 5 minutos de antecedência</li>
                    <li>Se precisares de cancelar, avisa com antecedência</li>
                    <li>Traz uma foto de referência se tiveres</li>
                    <li>Estacionamento disponível na rua</li>
                  </ul>
                </div>
                
                <div class="divider"></div>
                
                <p style="color:#9ca3af;font-size:14px;text-align:center;margin:0">
                  Precisas de alterar algo? <a href="mailto:geral@barbershop.pt" style="color:#C3FF5A;text-decoration:none;font-weight:600">Contacta-nos</a>
                </p>
              </div>
              
              <div class="footer">
                <strong style="color:#e9eef7">Barbershop</strong><br>
                Rua Principal, 123, Lisboa<br>
                (+351) 900 000 000<br><br>
                <a href="#" class="footer-link">Instagram</a> · 
                <a href="#" class="footer-link">Facebook</a> · 
                <a href="#" class="footer-link">Website</a>
              </div>
            </div>
          </td></tr>
        </table>
      </div>
    </body>
    </html>
    """, 
            clientName,     // %s 1
            barberName,     // %s 2
            serviceName,    // %s 3
            durationText,   // %s 4
            date,           // %s 5
            startTime,      // %s 6
            endTime,        // %s 7
            price,          // %s 8
            notes           // %s 9
        );
    }
}

