/*
	ALISON - Automatic Learning Intelligent Sentence Organising Network
    Copyright (C) 2023 ElementalMP4 (https://github.com/Elementalmp4/ALISON-V3)
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package main.java.de.voidtech.alison;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import main.java.de.voidtech.alison.listeners.MessageListener;
import main.java.de.voidtech.alison.listeners.ReadyListener;
import main.java.de.voidtech.alison.service.ConfigService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class Alison
{
    @Bean
    @Order(3)
    @Autowired
    public JDA getJDA(final MessageListener msgListener, final ReadyListener readyListener, EventWaiter waiter) throws LoginException, InterruptedException {
        final ConfigService config = new ConfigService();
        return JDABuilder.createLight(config.getToken()).enableIntents(getNonPrivilegedIntents())
        		.setMemberCachePolicy(MemberCachePolicy.ALL)
        		.setBulkDeleteSplittingEnabled(false)
        		.setStatus(OnlineStatus.IDLE)
        		.setActivity(Activity.competing("the game"))
        		.setCompression(Compression.NONE)
        		.addEventListeners(new Object[] { msgListener, readyListener, waiter })
        		.build()
        		.awaitReady();
    }
    
    private List<GatewayIntent> getNonPrivilegedIntents() {
        final List<GatewayIntent> gatewayIntents = new ArrayList<GatewayIntent>(Arrays.asList(GatewayIntent.values()));
        gatewayIntents.remove(GatewayIntent.GUILD_PRESENCES);
        return gatewayIntents;
    }
    
    @Bean
    public EventWaiter getEventWaiter() {
        return new EventWaiter();
    }
    
    public static void main(final String[] args) {
        SpringApplication.run(Alison.class, args);
    }
}
