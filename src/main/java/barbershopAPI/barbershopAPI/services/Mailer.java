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
        .wrapper{width:100%%;background:#0f1117;padding:40px 0}
        .container{max-width:600px;margin:0 auto;background:linear-gradient(135deg,#16181d 0%%,#1a1d27 100%%);border-radius:16px;overflow:hidden;box-shadow:0 8px 32px rgba(0,0,0,0.4)}
        .header{background:#C3FF5A;padding:32px 40px;text-align:center}
        .brand{font-size:28px;font-weight:800;color:#0f1117;margin:0;letter-spacing:-0.5px}
        .tagline{font-size:14px;color:rgba(15,17,23,0.7);margin:4px 0 0;font-weight:500}
        .content{padding:40px}
        .title{font-size:24px;font-weight:700;color:#e9eef7;margin:0 0 12px;line-height:1.3}
        .subtitle{color:#9ca3af;font-size:15px;line-height:1.6;margin:0 0 32px}
        .card{background:rgba(195,255,90,0.08);border:1px solid rgba(195,255,90,0.2);border-radius:12px;padding:24px;margin:0 0 32px}
        .info-row{display:flex;padding:12px 0;border-bottom:1px solid rgba(195,255,90,0.1)}
        .info-row:last-child{border-bottom:none}
        .info-label{color:#9ca3af;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:0.5px;min-width:110px;flex-shrink:0}
        .info-value{color:#e9eef7;font-size:15px;font-weight:500;flex:1}
        .highlight{color:#C3FF5A;font-weight:700}
        .cta{display:inline-block;background:#C3FF5A;color:#0f1117;text-decoration:none;padding:16px 32px;border-radius:10px;font-weight:700;font-size:15px;transition:all 0.2s;box-shadow:0 4px 12px rgba(195,255,90,0.3)}
        .cta:hover{background:#b3ef4a;transform:translateY(-2px);box-shadow:0 6px 20px rgba(195,255,90,0.4)}
        .divider{height:1px;background:linear-gradient(90deg,transparent,rgba(195,255,90,0.3),transparent);margin:32px 0}
        .note{background:rgba(251,191,36,0.1);border-left:4px solid #fbbf24;padding:16px;border-radius:8px;margin:24px 0}
        .note-label{color:#fbbf24;font-size:12px;font-weight:700;text-transform:uppercase;margin:0 0 8px}
        .note-text{color:#e9eef7;font-size:14px;margin:0;line-height:1.6}
        .footer{padding:32px 40px;text-align:center;color:#6b7280;font-size:13px;line-height:1.6}
        .footer-link{color:#9ca3af;text-decoration:none}
        .badge{display:inline-block;background:rgba(195,255,90,0.15);color:#C3FF5A;padding:6px 12px;border-radius:20px;font-size:12px;font-weight:700;margin:0 0 24px}
      </style>
    </head>
    <body>
      <div class="wrapper">
        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
          <tr><td align="center">
            <div class="container">
              <div class="header">
                <h1 class="brand">💈 BARBERSHOP</h1>
                <p class="tagline">Estilo & Tradição</p>
              </div>
              
              <div class="content">
                <div class="badge">✓ CONFIRMADA</div>
                <h2 class="title">A tua marcação está confirmada!</h2>
                <p class="subtitle">Preparámos tudo para te receber. Segue os detalhes da tua visita:</p>
                
                <div class="card">
                  <div class="info-row">
                    <div class="info-label">📅 Quando</div>
                    <div class="info-value highlight">%1$s</div>
                  </div>
                  <div class="info-row">
                    <div class="info-label">✂️ Serviço</div>
                    <div class="info-value">%2$s <span style="color:#9ca3af">(%3$d min)</span></div>
                  </div>
                  <div class="info-row">
                    <div class="info-label">👤 Barbeiro</div>
                    <div class="info-value">%4$s</div>
                  </div>
                  <div class="info-row">
                    <div class="info-label">🔖 Referência</div>
                    <div class="info-value">#%5$s</div>
                  </div>
                </div>
                
                %6$s
                
                <div style="text-align:center;margin:32px 0">
                  <a href="%7$s" class="cta">Ver marcação completa →</a>
                </div>
                
                <div class="divider"></div>
                
                <p style="color:#9ca3af;font-size:14px;text-align:center;margin:0">
                  Precisas de remarcar ou cancelar? <a href="mailto:geral@barbershop.pt" style="color:#C3FF5A;text-decoration:none">Contacta-nos</a>
                </p>
              </div>
              
              <div class="footer">
                <strong style="color:#e9eef7">Barbershop</strong><br>
                Rua Principal, 123 · Lisboa<br>
                📞 (+351) 900 000 000 · ✉️ geral@barbershop.pt<br><br>
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
                notes.isBlank() ? "" : "<div class=\"note\"><div class=\"note-label\">📝 Notas</div><p class=\"note-text\">" + escapeHtml(notes) + "</p></div>", // %6$s
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
            helper.setSubject("🔐 Confirma o teu email · Barbershop");

            var html = """
      <!doctype html>
      <html lang="pt">
      <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Confirma o teu email</title>
        <style>
          body{margin:0;padding:0;background:#0f1117;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif}
          .wrapper{width:100%%;background:#0f1117;padding:40px 0}
          .container{max-width:600px;margin:0 auto;background:linear-gradient(135deg,#16181d,#1a1d27);border-radius:16px;overflow:hidden;box-shadow:0 8px 32px rgba(0,0,0,0.4)}
          .header{background:#C3FF5A;padding:32px 40px;text-align:center}
          .brand{font-size:28px;font-weight:800;color:#0f1117;margin:0}
          .content{padding:40px;text-align:center}
          .icon{font-size:64px;margin:0 0 24px}
          .title{font-size:26px;font-weight:700;color:#e9eef7;margin:0 0 16px}
          .text{color:#9ca3af;font-size:16px;line-height:1.6;margin:0 0 32px}
          .cta{display:inline-block;background:#C3FF5A;color:#0f1117;text-decoration:none;padding:16px 40px;border-radius:10px;font-weight:700;font-size:16px;box-shadow:0 4px 12px rgba(195,255,90,0.3)}
          .cta:hover{background:#b3ef4a}
          .footer{padding:32px;text-align:center;color:#6b7280;font-size:13px}
          .note{background:rgba(59,130,246,0.1);border:1px solid rgba(59,130,246,0.2);border-radius:8px;padding:16px;margin:24px 0;color:#9ca3af;font-size:14px}
        </style>
      </head>
      <body>
        <div class="wrapper">
          <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
            <tr><td align="center">
              <div class="container">
                <div class="header">
                  <div class="brand">💈 BARBERSHOP</div>
                </div>
                <div class="content">
                  <div class="icon">🔐</div>
                  <h1 class="title">Confirma o teu email</h1>
                  <p class="text">Estás quase! Clica no botão abaixo para verificares o teu email e ativares a tua conta.</p>
                  <a href="%s" class="cta">Confirmar email →</a>
                  <div class="note">
                    ⏱️ Este link expira em <strong>24 horas</strong>
                  </div>
                  <p style="color:#6b7280;font-size:13px;margin:24px 0 0">Se não criaste esta conta, podes ignorar este email.</p>
                </div>
                <div class="footer">
                  <strong style="color:#e9eef7">Barbershop</strong><br>
                  📍 Rua Principal, 123 · Lisboa<br>
                  📞 (+351) 900 000 000
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
            helper.setSubject("👋 Bem-vindo à Barbershop, " + clientName + "!");

            var html = """
      <!doctype html>
      <html lang="pt">
      <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Define a tua senha</title>
        <style>
          body{margin:0;padding:0;background:#0f1117;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif}
          .wrapper{width:100%%;background:#0f1117;padding:40px 0}
          .container{max-width:600px;margin:0 auto;background:linear-gradient(135deg,#16181d,#1a1d27);border-radius:16px;overflow:hidden;box-shadow:0 8px 32px rgba(0,0,0,0.4)}
          .header{background:#C3FF5A;padding:32px 40px;text-align:center}
          .brand{font-size:28px;font-weight:800;color:#0f1117;margin:0}
          .content{padding:40px}
          .greeting{font-size:32px;font-weight:700;color:#e9eef7;margin:0 0 16px;text-align:center}
          .welcome{background:rgba(195,255,90,0.08);border:1px solid rgba(195,255,90,0.2);border-radius:12px;padding:24px;margin:0 0 32px}
          .welcome-title{color:#C3FF5A;font-size:14px;font-weight:700;text-transform:uppercase;margin:0 0 12px;letter-spacing:1px}
          .welcome-text{color:#9ca3af;font-size:15px;line-height:1.6;margin:0}
          .steps{margin:32px 0}
          .step{display:flex;align-items:flex-start;margin:16px 0;padding:16px;background:rgba(0,0,0,0.2);border-radius:8px}
          .step-number{background:#C3FF5A;color:#0f1117;font-weight:800;width:32px;height:32px;border-radius:50%%;display:flex;align-items:center;justify-content:center;flex-shrink:0;margin-right:16px;font-size:16px}
          .step-text{color:#e9eef7;font-size:14px;line-height:1.6;flex:1;padding-top:4px}
          .cta-container{text-align:center;margin:32px 0}
          .cta{display:inline-block;background:#C3FF5A;color:#0f1117;text-decoration:none;padding:18px 48px;border-radius:12px;font-weight:800;font-size:16px;box-shadow:0 4px 12px rgba(195,255,90,0.3);transition:all 0.2s}
          .cta:hover{background:#b3ef4a;transform:translateY(-2px);box-shadow:0 6px 20px rgba(195,255,90,0.4)}
          .expiry{background:rgba(239,68,68,0.1);border:1px solid rgba(239,68,68,0.2);border-radius:8px;padding:16px;margin:24px 0;text-align:center}
          .expiry-text{color:#f87171;font-size:14px;font-weight:600;margin:0}
          .footer{padding:32px;text-align:center;color:#6b7280;font-size:13px;line-height:1.8}
        </style>
      </head>
      <body>
        <div class="wrapper">
          <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
            <tr><td align="center">
              <div class="container">
                <div class="header">
                  <div class="brand">💈 BARBERSHOP</div>
                </div>
                <div class="content">
                  <div class="greeting">Olá, %1$s! 👋</div>
                  
                  <div class="welcome">
                    <div class="welcome-title">✨ Bem-vindo à família</div>
                    <p class="welcome-text">Criámos uma conta para ti. Agora falta apenas um passo para começares a marcar os teus serviços e gerir as tuas visitas.</p>
                  </div>
                  
                  <div class="steps">
                    <div class="step">
                      <div class="step-number">1</div>
                      <div class="step-text">Clica no botão "Definir Senha" abaixo</div>
                    </div>
                    <div class="step">
                      <div class="step-number">2</div>
                      <div class="step-text">Cria uma senha segura (mínimo 8 caracteres)</div>
                    </div>
                    <div class="step">
                      <div class="step-number">3</div>
                      <div class="step-text">Faz login e começa a marcar os teus cortes!</div>
                    </div>
                  </div>
                  
                  <div class="cta-container">
                    <a href="%2$s" class="cta">Definir Senha →</a>
                  </div>
                  
                  <div class="expiry">
                    <p class="expiry-text">⏱️ Este link expira em 48 horas</p>
                  </div>
                  
                  <p style="color:#6b7280;font-size:13px;text-align:center;margin:24px 0 0">Se não pediste esta conta, podes ignorar este email em segurança.</p>
                </div>
                <div class="footer">
                  <strong style="color:#e9eef7">Barbershop</strong><br>
                  📍 Rua Principal, 123 · Lisboa<br>
                  📞 (+351) 900 000 000 · ✉️ geral@barbershop.pt<br><br>
                  Segue-nos nas redes sociais 👇<br>
                  <a href="#" style="color:#9ca3af;text-decoration:none">Instagram</a> · 
                  <a href="#" style="color:#9ca3af;text-decoration:none">Facebook</a>
                </div>
              </div>
            </td></tr>
          </table>
        </div>
      </body>
      </html>
    """.formatted(clientName, setPasswordLink);

            var text = """
      Olá, %1$s! 👋
      
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
