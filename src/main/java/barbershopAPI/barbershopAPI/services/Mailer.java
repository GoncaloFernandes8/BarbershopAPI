// Mailer.java
package barbershopAPI.barbershopAPI.services;

import jakarta.activation.DataSource;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import barbershopAPI.barbershopAPI.entities.Appointment;
import barbershopAPI.barbershopAPI.entities.ServiceEntity;
import barbershopAPI.barbershopAPI.entities.Barber;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class Mailer {

    private final JavaMailSender mailSender;

    @Value("${MAIL_FROM:no-reply@barbearia.local}")
    String fromEmail;

    @Value("${MAIL_FROM_NAME:Barbearia}")
    String fromName;

    @Value("${FRONTEND_BASE_URL:http://localhost:4200}")
    String frontendBaseUrl;

    private static final ZoneId TZ = ZoneId.of("Europe/Lisbon");
    private static final Locale PT = new Locale("pt", "PT");

    @Async
    public void sendAppointmentConfirmation(String to, Appointment appt, ServiceEntity svc, Barber barber) {
        try {
            var whenLisbon = appt.getStartsAt().atZoneSameInstant(TZ);
            var endsLisbon = appt.getEndsAt() == null ? null : appt.getEndsAt().atZoneSameInstant(TZ);

            var dateHuman = whenLisbon.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'às' HH:mm", PT));
            var subject = "✅ Marcação confirmada – " + dateHuman;

            var successUrl = frontendBaseUrl.replaceAll("/+$", "")
                    + "/sucesso/" + appt.getId().toString();

            // 1) Construir HTML + plain-text
            String html = buildHtmlEmail(appt, svc, barber, dateHuman, successUrl);
            String text = buildPlainEmail(appt, svc, barber, dateHuman, successUrl);

            // 2) Mime com HTML (e alternativa texto)
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(fromEmail, fromName));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, html); // plain + html

            // 3) (Opcional) .ics “Adicionar ao calendário”
            byte[] ics = buildIcs(appt, svc, whenLisbon, endsLisbon);
            DataSource ds = new ByteArrayDataSource(ics, "text/calendar; charset=UTF-8");
            helper.addAttachment("marcacao-" + appt.getId() + ".ics", ds);

            mailSender.send(mime);
            log.info("Email de confirmação enviado para {} (appt {})", to, appt.getId());
        } catch (Exception ex) {
            log.warn("Falha ao enviar email de confirmação para {} (appt {}): {}",
                    to, appt.getId(), ex.getMessage(), ex);
        }
    }

    private String buildHtmlEmail(Appointment appt, ServiceEntity svc, Barber barber, String dateHuman, String successUrl) {
        String id = appt.getId().toString();
        String notes = appt.getNotes() == null ? "" : appt.getNotes();

        return """
    <!doctype html>
    <html lang="pt">
    <head>
      <meta charset="utf-8">
      <meta name="x-apple-disable-message-reformatting">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>Marcação confirmada</title>
      <style>
        body{margin:0;padding:0;background:#0f1117;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif}
        .wrapper{width:100%%;background:#0f1117;padding:40px 20px}
        .container{max-width:600px;margin:0 auto;background:#16181d;border:1px solid #2a3042;border-radius:16px;overflow:hidden}
        .header{background:#C3FF5A;padding:40px;text-align:center}
        .logo-icon{width:48px;height:48px;margin:0 auto 12px;color:#0f1117}
        .brand{font-size:26px;font-weight:800;color:#0f1117;margin:0;letter-spacing:-0.5px}
        .tagline{font-size:13px;color:rgba(15,17,23,0.6);margin:6px 0 0;font-weight:500}
        .content{padding:40px}
        .status-badge{display:inline-flex;align-items:center;gap:8px;background:rgba(195,255,90,0.15);border:1px solid rgba(195,255,90,0.3);color:#C3FF5A;padding:10px 20px;border-radius:24px;font-size:13px;font-weight:700;margin:0 0 28px;letter-spacing:0.5px}
        .status-icon{width:18px;height:18px;color:#C3FF5A}
        .title{font-size:24px;font-weight:700;color:#e9eef7;margin:0 0 12px;line-height:1.3}
        .subtitle{color:#9ca3af;font-size:15px;line-height:1.6;margin:0 0 32px}
        .info-card{background:rgba(0,0,0,0.3);border:1px solid #2a3042;border-radius:12px;padding:0;margin:0 0 32px;overflow:hidden}
        .info-row{display:flex;align-items:center;padding:18px 20px;border-bottom:1px solid #2a3042}
        .info-row:last-child{border-bottom:none}
        .info-icon{color:#C3FF5A;margin-right:14px;display:flex;align-items:center;flex-shrink:0}
        .info-label{color:#6b7280;font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:0.8px;min-width:100px;flex-shrink:0}
        .info-value{color:#e9eef7;font-size:16px;font-weight:500;flex:1}
        .highlight{color:#C3FF5A;font-weight:700}
        .cta-container{text-align:center;margin:32px 0}
        .cta{display:inline-block;background:#C3FF5A !important;color:#0f1117 !important;text-decoration:none !important;padding:18px 40px;border-radius:12px;font-weight:800;font-size:16px;box-shadow:0 4px 16px rgba(195,255,90,0.3);transition:all 0.2s}
        .cta:visited{color:#0f1117 !important}
        .cta:hover{color:#0f1117 !important}
        .cta:active{color:#0f1117 !important}
        .divider{height:1px;background:linear-gradient(90deg,transparent,rgba(195,255,90,0.2),transparent);margin:32px 0}
        .note-card{background:rgba(195,255,90,0.05);border-left:4px solid #C3FF5A;padding:20px;border-radius:8px;margin:24px 0}
        .note-icon{color:#C3FF5A;margin-bottom:8px}
        .note-label{color:#C3FF5A;font-size:11px;font-weight:700;text-transform:uppercase;margin:0 0 10px;letter-spacing:1px}
        .note-text{color:#cbd4e6;font-size:14px;margin:0;line-height:1.6}
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
                <div class="status-badge">
                  <svg class="status-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                    <path d="M20 6L9 17l-5-5"/>
                  </svg>
                  CONFIRMADA
                </div>
                <h2 class="title">A tua marcação está confirmada</h2>
                <p class="subtitle">Preparámos tudo para te receber. Aqui estão os detalhes da tua visita:</p>
                
                <div class="info-card">
                  <div class="info-row">
                    <div class="info-icon">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/>
                      </svg>
                    </div>
                    <div class="info-label">Quando</div>
                    <div class="info-value highlight">%1$s</div>
                  </div>
                  <div class="info-row">
                    <div class="info-icon">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="6" cy="6" r="3"/><circle cx="6" cy="18" r="3"/>
                        <path d="M20 4L8.12 15.88M14.47 14.48L20 20M8.12 8.12L12 12"/>
                      </svg>
                    </div>
                    <div class="info-label">Serviço</div>
                    <div class="info-value">%2$s <span style="color:#6b7280">(%3$d min)</span></div>
                  </div>
                  <div class="info-row">
                    <div class="info-icon">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>
                      </svg>
                    </div>
                    <div class="info-label">Barbeiro</div>
                    <div class="info-value">%4$s</div>
                  </div>
                  <div class="info-row">
                    <div class="info-icon">
                      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
                        <circle cx="7" cy="7" r="1"/>
                      </svg>
                    </div>
                    <div class="info-label">Referência</div>
                    <div class="info-value">#%5$s</div>
                  </div>
                </div>
                
                %6$s
                
                <div class="cta-container">
                  <a href="%7$s" class="cta" style="color:#0f1117 !important;background:#C3FF5A !important;text-decoration:none !important">
                    <span style="color:#0f1117 !important">Ver marcação completa</span>
                  </a>
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
    """.formatted(
                dateHuman,                         // %1$s
                svc.getName(),                     // %2$s
                svc.getDurationMin(),              // %3$d
                barber.getName(),                  // %4$s
                id,                                // %5$s
                notes.isBlank() ? "" : "<div class=\"note-card\"><svg class=\"note-icon\" width=\"18\" height=\"18\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\"><path d=\"M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z\"/><path d=\"M14 2v6h6M16 13H8M16 17H8M10 9H8\"/></svg><div class=\"note-label\">Notas adicionais</div><p class=\"note-text\">" + escapeHtml(notes) + "</p></div>", // %6$s
                successUrl                         // %7$s
        );
    }

    private String buildPlainEmail(Appointment appt, ServiceEntity svc, Barber barber, String dateHuman, String successUrl) {
        return """
      Marcação confirmada

      • Data e hora: %s
      • Serviço: %s (%d min)
      • Barbeiro: %s
      • Nº da marcação: #%s

      Ver online: %s

      Até já!
      """.formatted(
                dateHuman, svc.getName(), svc.getDurationMin(),
                barber.getName(), appt.getId().toString(), successUrl
        );
    }

    private byte[] buildIcs(Appointment appt, ServiceEntity svc, ZonedDateTime startLisbon, ZonedDateTime endLisbon) {
        // Evento em UTC para interoperabilidade
        var startUtc = startLisbon.withZoneSameInstant(ZoneOffset.UTC);
        var endUtc   = (endLisbon == null ? startLisbon.plusMinutes(svc.getDurationMin()) : endLisbon).withZoneSameInstant(ZoneOffset.UTC);

        String uid = appt.getId().toString() + "@barbearia";
        String summary = "Barbearia – " + svc.getName();
        String desc = "Marcação #" + appt.getId();

        String ics = """
      BEGIN:VCALENDAR
      VERSION:2.0
      PRODID:-//Barbearia//Appointments//PT
      CALSCALE:GREGORIAN
      METHOD:PUBLISH
      BEGIN:VEVENT
      UID:%s
      DTSTAMP:%sZ
      DTSTART:%sZ
      DTEND:%sZ
      SUMMARY:%s
      DESCRIPTION:%s
      END:VEVENT
      END:VCALENDAR
      """.formatted(
                uid,
                DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").format(ZonedDateTime.now(ZoneOffset.UTC)),
                DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").format(startUtc),
                DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").format(endUtc),
                escapeIcs(summary),
                escapeIcs(desc)
        );

        return ics.getBytes(StandardCharsets.UTF_8);
    }

    private String escapeHtml(String s){
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
    private String escapeIcs(String s){
        return s.replace("\\","\\\\").replace("\n","\\n").replace(",","\\,").replace(";","\\;");
    }




    @Async
    public void sendEmailVerification(String to, String verifyLink){
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(fromEmail, fromName));
            helper.setTo(to);
            helper.setSubject("Confirma o teu email · Barbershop");

            var html = """
      <!doctype html>
      <html lang="pt">
      <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Confirma o teu email</title>
        <style>
          body{margin:0;padding:0;background:#0f1117;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif}
          .wrapper{width:100%%;background:#0f1117;padding:40px 20px}
          .container{max-width:600px;margin:0 auto;background:#16181d;border:1px solid #2a3042;border-radius:16px;overflow:hidden}
          .header{background:#C3FF5A;padding:40px;text-align:center}
          .logo-icon{width:48px;height:48px;margin:0 auto 12px;color:#0f1117}
          .brand{font-size:26px;font-weight:800;color:#0f1117;margin:0;letter-spacing:-0.5px}
          .tagline{font-size:13px;color:rgba(15,17,23,0.6);margin:6px 0 0;font-weight:500}
          .content{padding:40px;text-align:center}
          .hero-icon{width:80px;height:80px;margin:0 auto 28px;color:#C3FF5A}
          .title{font-size:26px;font-weight:700;color:#e9eef7;margin:0 0 16px}
          .text{color:#9ca3af;font-size:16px;line-height:1.6;margin:0 0 36px;max-width:480px;margin-left:auto;margin-right:auto}
          .cta-container{text-align:center;margin:36px 0}
          .cta{display:inline-block;background:#C3FF5A !important;color:#0f1117 !important;text-decoration:none !important;padding:18px 48px;border-radius:12px;font-weight:800;font-size:16px;box-shadow:0 4px 16px rgba(195,255,90,0.3)}
          .cta:visited{color:#0f1117 !important}
          .cta:hover{color:#0f1117 !important}
          .cta:active{color:#0f1117 !important}
          .warning{background:rgba(195,255,90,0.08);border:1px solid rgba(195,255,90,0.2);border-radius:10px;padding:18px;margin:28px auto 0;max-width:400px;display:flex;align-items:center;justify-content:center;gap:10px}
          .warning-icon{color:#C3FF5A;flex-shrink:0}
          .warning-text{color:#cbd4e6;font-size:14px;font-weight:500;margin:0}
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
                  <div class="brand">BARBERSHOP</div>
                  <p class="tagline">Estilo & Tradição</p>
                </div>
                <div class="content">
                  <svg class="hero-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                  </svg>
                  <h1 class="title">Confirma o teu email</h1>
                  <p class="text">Estás quase lá! Clica no botão abaixo para verificar o teu email e ativar a tua conta na Barbershop.</p>
                  <div class="cta-container">
                    <a href="%s" class="cta" style="color:#0f1117 !important;background:#C3FF5A !important;text-decoration:none !important">
                      <span style="color:#0f1117 !important">Confirmar email</span>
                    </a>
                  </div>
                  <div class="warning">
                    <svg class="warning-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
                    </svg>
                    <p class="warning-text">Este link expira em 24 horas</p>
                  </div>
                  <p style="color:#6b7280;font-size:13px;margin:28px 0 0">Se não criaste esta conta, ignora este email.</p>
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
    """.formatted(verifyLink);

            var text = """
      Confirma o teu email
      
      Estás quase! Para ativares a tua conta, confirma o teu email:
      %s
      
      Este link expira em 24 horas.
      
      Barbershop
    """.formatted(verifyLink);

            helper.setText(text, html);
            mailSender.send(mime);
            log.info("Email de verificação enviado para {}", to);
        } catch (Exception ex) {
            log.warn("Falha a enviar email de verificação para {}: {}", to, ex.getMessage(), ex);
        }
    }

    @Async
    public void sendSetPasswordEmail(String to, String clientName, String setPasswordLink){
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(fromEmail, fromName));
            helper.setTo(to);
            helper.setSubject("Bem-vindo à Barbershop, " + clientName);

            var html = """
      <!doctype html>
      <html lang="pt">
      <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Define a tua senha</title>
        <style>
          body{margin:0;padding:0;background:#0f1117;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif}
          .wrapper{width:100%%;background:#0f1117;padding:40px 20px}
          .container{max-width:600px;margin:0 auto;background:#16181d;border:1px solid #2a3042;border-radius:16px;overflow:hidden}
          .header{background:#C3FF5A;padding:40px;text-align:center}
          .logo-icon{width:48px;height:48px;margin:0 auto 12px;color:#0f1117}
          .brand{font-size:26px;font-weight:800;color:#0f1117;margin:0;letter-spacing:-0.5px}
          .tagline{font-size:13px;color:rgba(15,17,23,0.6);margin:6px 0 0;font-weight:500}
          .content{padding:40px}
          .hero-icon{width:80px;height:80px;margin:0 auto 28px;color:#C3FF5A}
          .title{font-size:26px;font-weight:700;color:#e9eef7;margin:0 0 16px;text-align:center}
          .subtitle{color:#9ca3af;font-size:16px;line-height:1.6;margin:0 0 36px;text-align:center;max-width:480px;margin-left:auto;margin-right:auto}
          .welcome-card{background:rgba(195,255,90,0.08);border:1px solid rgba(195,255,90,0.2);border-radius:12px;padding:28px;margin:0 0 32px}
          .welcome-title{color:#C3FF5A;font-size:13px;font-weight:700;text-transform:uppercase;margin:0 0 14px;letter-spacing:1.2px;text-align:center}
          .welcome-text{color:#cbd4e6;font-size:15px;line-height:1.7;margin:0;text-align:center}
          .steps{margin:32px 0;background:rgba(0,0,0,0.3);border:1px solid #2a3042;border-radius:12px;padding:24px}
          .step{display:flex;align-items:center;margin:18px 0;gap:16px}
          .step-number{background:#C3FF5A;color:#0f1117;font-weight:800;width:36px;height:36px;border-radius:50%%;display:flex;align-items:center;justify-content:center;flex-shrink:0;font-size:16px}
          .step-text{color:#e9eef7;font-size:15px;font-weight:500;flex:1}
          .cta-container{text-align:center;margin:36px 0}
          .cta{display:inline-block;background:#C3FF5A !important;color:#0f1117 !important;text-decoration:none !important;padding:18px 48px;border-radius:12px;font-weight:800;font-size:16px;box-shadow:0 4px 16px rgba(195,255,90,0.3)}
          .cta:visited{color:#0f1117 !important}
          .cta:hover{color:#0f1117 !important}
          .cta:active{color:#0f1117 !important}
          .warning{background:rgba(195,255,90,0.08);border:1px solid rgba(195,255,90,0.2);border-radius:10px;padding:18px;margin:28px auto 0;max-width:400px;display:flex;align-items:center;justify-content:center;gap:10px}
          .warning-icon{color:#C3FF5A;flex-shrink:0}
          .warning-text{color:#cbd4e6;font-size:14px;font-weight:500;margin:0}
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
                  <div class="brand">BARBERSHOP</div>
                  <p class="tagline">Estilo & Tradição</p>
                </div>
                <div class="content">
                  <svg class="hero-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>
                  </svg>
                  <h1 class="title">Olá, %1$s!</h1>
                  <p class="subtitle">Bem-vindo à Barbershop. Criámos uma conta para ti.</p>
                  
                  <div class="welcome-card">
                    <div class="welcome-title">Como começar</div>
                    <p class="welcome-text">Falta apenas definires a tua senha para teres acesso completo à plataforma e poderes gerir as tuas marcações.</p>
                  </div>
                  
                  <div class="steps">
                    <div class="step">
                      <div class="step-number">1</div>
                      <div class="step-text">Clica no botão abaixo</div>
                    </div>
                    <div class="step">
                      <div class="step-number">2</div>
                      <div class="step-text">Define uma senha segura (mínimo 8 caracteres)</div>
                    </div>
                    <div class="step">
                      <div class="step-number">3</div>
                      <div class="step-text">Acede à tua conta e marca o teu próximo corte</div>
                    </div>
                  </div>
                  
                  <div class="cta-container">
                    <a href="%2$s" class="cta" style="color:#0f1117 !important;background:#C3FF5A !important;text-decoration:none !important">
                      <span style="color:#0f1117 !important">Definir senha</span>
                    </a>
                  </div>
                  
                  <div class="warning">
                    <svg class="warning-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>
                    </svg>
                    <p class="warning-text">Este link expira em 48 horas</p>
                  </div>
                  
                  <p style="color:#6b7280;font-size:13px;text-align:center;margin:28px 0 0">Se não pediste esta conta, ignora este email.</p>
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
    """.formatted(clientName, setPasswordLink);

            var text = """
      Olá, %1$s!
      
      Bem-vindo à Barbershop!
      
      Criámos uma conta para ti. Para começares a usar, define a tua senha:
      %2$s
      
      Este link expira em 48 horas.
      
      Até já!
      Barbershop
    """.formatted(clientName, setPasswordLink);

            helper.setText(text, html);
            mailSender.send(mime);
            log.info("Email de definição de senha enviado para {}", to);
        } catch (Exception ex) {
            log.warn("Falha a enviar email de definição de senha para {}: {}", to, ex.getMessage(), ex);
        }
    }

}
