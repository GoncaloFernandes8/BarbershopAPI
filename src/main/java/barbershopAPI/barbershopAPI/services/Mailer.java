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
            var subject = "Marcação confirmada – " + dateHuman;

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
        String notesHtml = appt.getNotes() != null && !appt.getNotes().isBlank() 
            ? String.format("""
                <tr>
                  <td bgcolor="#ffffff" style="padding:0 40px 24px 40px">
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#fffbeb" style="border-left:4px solid #C3FF5A;border-radius:8px">
                      <tr>
                        <td style="padding:20px">
                          <div style="font-size:11px;font-weight:700;color:#92400e;text-transform:uppercase;letter-spacing:1px;margin-bottom:10px;font-family:Arial,sans-serif">Notas adicionais</div>
                          <div style="font-size:14px;color:#374151;font-family:Arial,sans-serif;line-height:1.6">%s</div>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
                """, escapeHtml(appt.getNotes()))
            : "";

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
        
        <!-- Badge -->
        <tr>
          <td bgcolor="#ffffff" style="padding:32px 40px 0 40px;text-align:center">
            <div style="display:inline-block;background-color:#f0fdf4;border:2px solid #C3FF5A;border-radius:24px;padding:10px 24px">
              <span style="font-size:13px;font-weight:700;color:#0f1117;font-family:Arial,sans-serif;letter-spacing:0.5px">✓ CONFIRMADA</span>
            </div>
          </td>
        </tr>
        
        <!-- Título -->
        <tr>
          <td bgcolor="#ffffff" style="padding:24px 40px 12px 40px">
            <div style="font-size:24px;font-weight:700;color:#111111;font-family:Arial,sans-serif">A tua marcação está confirmada</div>
              </td>
            </tr>
            <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 32px 40px">
            <div style="font-size:15px;color:#666666;font-family:Arial,sans-serif;line-height:1.6">Preparámos tudo para te receber. Aqui estão os detalhes da tua visita:</div>
          </td>
        </tr>
        
        <!-- Info Card -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 24px 40px">
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#f9fafb" style="border:1px solid #e5e7eb;border-radius:12px;overflow:hidden">
              
              <!-- Quando -->
              <tr>
                <td style="padding:18px 24px;border-bottom:1px solid #e5e7eb">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                    <tr>
                      <td width="110" style="font-size:12px;font-weight:700;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">Quando</td>
                      <td style="font-size:16px;font-weight:700;color:#0f1117;font-family:Arial,sans-serif">%s</td>
                    </tr>
                  </table>
                </td>
              </tr>
              
              <!-- Serviço -->
              <tr>
                <td style="padding:18px 24px;border-bottom:1px solid #e5e7eb">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                    <tr>
                      <td width="110" style="font-size:12px;font-weight:700;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">Serviço</td>
                      <td style="font-size:16px;font-weight:600;color:#111111;font-family:Arial,sans-serif">%s <span style="color:#6b7280;font-weight:400">(%d min)</span></td>
                    </tr>
                  </table>
                </td>
              </tr>
              
              <!-- Barbeiro -->
              <tr>
                <td style="padding:18px 24px;border-bottom:1px solid #e5e7eb">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                    <tr>
                      <td width="110" style="font-size:12px;font-weight:700;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">Barbeiro</td>
                      <td style="font-size:16px;font-weight:600;color:#111111;font-family:Arial,sans-serif">%s</td>
                    </tr>
                  </table>
                </td>
              </tr>
              
              <!-- Referência -->
              <tr>
                <td style="padding:18px 24px">
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                    <tr>
                      <td width="110" style="font-size:12px;font-weight:700;color:#6b7280;text-transform:uppercase;font-family:Arial,sans-serif;letter-spacing:0.5px">Referência</td>
                      <td style="font-size:16px;font-weight:600;color:#111111;font-family:Arial,sans-serif">#%s</td>
                    </tr>
                  </table>
                </td>
              </tr>
              
            </table>
          </td>
        </tr>
        
        %s
        
        <!-- Botão -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 24px 40px;text-align:center">
            <a href="%s" style="display:inline-block;background-color:#C3FF5A;color:#0f1117;text-decoration:none;padding:16px 40px;border-radius:12px;font-weight:800;font-size:16px;font-family:Arial,sans-serif;box-shadow:0 2px 8px rgba(195,255,90,0.3)">Ver marcação completa</a>
          </td>
        </tr>
        
        <!-- Divider -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 24px 40px">
            <div style="height:1px;background-color:#e5e7eb"></div>
          </td>
        </tr>
        
        <!-- Contacto -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 40px 40px;text-align:center">
            <div style="font-size:14px;color:#666666;font-family:Arial,sans-serif">
              Precisas de alterar algo? <a href="mailto:geral@barbershop.pt" style="color:#0f1117;text-decoration:none;font-weight:700;border-bottom:2px solid #C3FF5A">Contacta-nos</a>
            </div>
              </td>
            </tr>
        
        <!-- Footer -->
        <tr>
          <td bgcolor="#111111" style="padding:40px;text-align:center">
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
                dateHuman,           // %s
                svc.getName(),       // %s
                svc.getDurationMin(),// %d
                barber.getName(),    // %s
                id,                  // %s
                notesHtml,           // %s
                successUrl           // %s
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

            var html = String.format("""
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
        
        <!-- Título -->
        <tr>
          <td bgcolor="#ffffff" style="padding:48px 40px 16px 40px;text-align:center">
            <div style="font-size:26px;font-weight:700;color:#111111;font-family:Arial,sans-serif">Confirma o teu email</div>
          </td>
        </tr>
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 36px 40px;text-align:center">
            <div style="font-size:16px;color:#666666;font-family:Arial,sans-serif;line-height:1.6;max-width:400px;margin:0 auto">Estás quase lá! Clica no botão abaixo para verificar o teu email e ativar a tua conta.</div>
          </td>
        </tr>
        
        <!-- Botão -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 24px 40px;text-align:center">
            <a href="%s" style="display:inline-block;background-color:#C3FF5A;color:#0f1117;text-decoration:none;padding:16px 48px;border-radius:12px;font-weight:800;font-size:16px;font-family:Arial,sans-serif;box-shadow:0 2px 8px rgba(195,255,90,0.3)">Confirmar email</a>
          </td>
        </tr>
        
        <!-- Aviso -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 40px 40px">
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#f0fdf4" style="border:2px solid #C3FF5A;border-radius:12px;overflow:hidden">
              <tr>
                <td align="center" style="padding:18px;font-size:14px;color:#374151;font-family:Arial,sans-serif">
                  <span style="color:#0f1117;font-weight:700">●</span> Este link expira em <strong style="color:#0f1117">24 horas</strong>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        
        <!-- Nota -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 40px 40px;text-align:center">
            <div style="font-size:13px;color:#6b7280;font-family:Arial,sans-serif">Se não criaste esta conta, ignora este email.</div>
          </td>
        </tr>
        
        <!-- Footer -->
        <tr>
          <td bgcolor="#111111" style="padding:40px;text-align:center">
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
    """, verifyLink);

            var text = String.format("""
Confirma o teu email

Estás quase! Para ativares a tua conta, confirma o teu email:
%s

Este link expira em 24 horas.

Barbershop
    """, verifyLink);

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

            var html = String.format("""
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
        
        <!-- Saudação -->
        <tr>
          <td bgcolor="#ffffff" style="padding:48px 40px 16px 40px;text-align:center">
            <div style="font-size:28px;font-weight:700;color:#111111;font-family:Arial,sans-serif">Olá, %s!</div>
          </td>
        </tr>
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 32px 40px;text-align:center">
            <div style="font-size:16px;color:#666666;font-family:Arial,sans-serif">Bem-vindo à Barbershop. Criámos uma conta para ti.</div>
          </td>
        </tr>
        
        <!-- Welcome Card -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 24px 40px">
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#f0fdf4" style="border:2px solid #C3FF5A;border-radius:12px;overflow:hidden">
              <tr>
                <td style="padding:24px;text-align:center">
                  <div style="font-size:12px;font-weight:700;color:#0f1117;text-transform:uppercase;letter-spacing:1.2px;margin-bottom:12px;font-family:Arial,sans-serif">Como começar</div>
                  <div style="font-size:15px;color:#374151;font-family:Arial,sans-serif;line-height:1.7">Falta apenas definires a tua senha para teres acesso completo à plataforma.</div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        
        <!-- Steps -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 24px 40px">
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#f9fafb" style="border:1px solid #e5e7eb;border-radius:12px;overflow:hidden">
              <tr>
                <td style="padding:24px">
                  <!-- Step 1 -->
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin-bottom:16px">
                    <tr>
                      <td width="50" valign="top">
                        <div style="background-color:#C3FF5A;color:#0f1117;font-weight:800;width:40px;height:40px;border-radius:50%%;text-align:center;line-height:40px;font-size:16px;font-family:Arial,sans-serif">1</div>
                      </td>
                      <td style="color:#111111;font-size:15px;font-family:Arial,sans-serif;padding-left:12px;vertical-align:middle;font-weight:500">Clica no botão abaixo</td>
                    </tr>
                  </table>
                  
                  <!-- Step 2 -->
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin-bottom:16px">
                    <tr>
                      <td width="50" valign="top">
                        <div style="background-color:#C3FF5A;color:#0f1117;font-weight:800;width:40px;height:40px;border-radius:50%%;text-align:center;line-height:40px;font-size:16px;font-family:Arial,sans-serif">2</div>
                      </td>
                      <td style="color:#111111;font-size:15px;font-family:Arial,sans-serif;padding-left:12px;vertical-align:middle;font-weight:500">Define uma senha segura (mínimo 8 caracteres)</td>
                    </tr>
                  </table>
                  
                  <!-- Step 3 -->
                  <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                    <tr>
                      <td width="50" valign="top">
                        <div style="background-color:#C3FF5A;color:#0f1117;font-weight:800;width:40px;height:40px;border-radius:50%%;text-align:center;line-height:40px;font-size:16px;font-family:Arial,sans-serif">3</div>
                      </td>
                      <td style="color:#111111;font-size:15px;font-family:Arial,sans-serif;padding-left:12px;vertical-align:middle;font-weight:500">Acede à tua conta e marca o teu próximo corte</td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        
        <!-- Botão -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 24px 40px;text-align:center">
            <a href="%s" style="display:inline-block;background-color:#C3FF5A;color:#0f1117;text-decoration:none;padding:16px 48px;border-radius:12px;font-weight:800;font-size:16px;font-family:Arial,sans-serif;box-shadow:0 2px 8px rgba(195,255,90,0.3)">Definir senha</a>
          </td>
        </tr>
        
        <!-- Aviso -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 40px 40px">
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" bgcolor="#f0fdf4" style="border:2px solid #C3FF5A;border-radius:12px;overflow:hidden">
              <tr>
                <td align="center" style="padding:18px;font-size:14px;color:#374151;font-family:Arial,sans-serif">
                  <span style="color:#0f1117;font-weight:700">●</span> Este link expira em <strong style="color:#0f1117">48 horas</strong>
                </td>
              </tr>
            </table>
          </td>
        </tr>
        
        <!-- Nota -->
        <tr>
          <td bgcolor="#ffffff" style="padding:0 40px 40px 40px;text-align:center">
            <div style="font-size:13px;color:#6b7280;font-family:Arial,sans-serif">Se não pediste esta conta, ignora este email.</div>
          </td>
        </tr>
        
        <!-- Footer -->
        <tr>
          <td bgcolor="#111111" style="padding:40px;text-align:center">
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
            """, clientName, setPasswordLink);

            var text = String.format("""
Olá, %s!

Bem-vindo à Barbershop!

Criámos uma conta para ti. Para começares a usar, define a tua senha:
%s

Este link expira em 48 horas.

Barbershop
    """, clientName, setPasswordLink);

            helper.setText(text, html);
            mailSender.send(mime);
            log.info("Email de definição de senha enviado para {}", to);
        } catch (Exception ex) {
            log.warn("Falha a enviar email de definição de senha para {}: {}", to, ex.getMessage(), ex);
        }
    }

}
