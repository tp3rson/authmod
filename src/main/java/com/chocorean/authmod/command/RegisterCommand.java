package com.chocorean.authmod.command;

import com.chocorean.authmod.Handler;
import com.chocorean.authmod.PlayerDescriptor;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class RegisterCommand implements ICommand {
    private final ArrayList aliases;

    public RegisterCommand(){
        aliases = new ArrayList();
        aliases.add("register");
        aliases.add("reg");
    }

    @Override
    public String getCommandName() {
        return "register";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/register <password> <password>  - Be careful when choosing it, you'll be asked to login each time you play..";
    }

    @Override
    public List<String> getCommandAliases() {
        return aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        // checking syntax
        if (args.length!=2) {
            sender.addChatMessage(new TextComponentString("Invalid number of arguments."));
            return;
        }
        // checking if password match and processing
        if (args[0].equals(args[1])){
            // Check if player has already register
            String hash = generateHash(sender.getName());
            try {
                BufferedReader br = new BufferedReader(new FileReader("mods/AuthMod/data"));
                String line=br.readLine();

                while (line != null) {
                    if (line.contains(hash)) {
                        sender.addChatMessage(new TextComponentString("You have already registered."));
                        return;
                    }
                    line = br.readLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // if player has not registered yet
            PrintWriter pw ;
            try {
                pw = new PrintWriter(new FileWriter("mods/AuthMod/data", true));
                pw.write(generateHash(sender.getName())+" "+ generateHash(args[0])+"\n");
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Free player here
            for (PlayerDescriptor dc : Handler.desc) {
                if (dc.getPlayer().getName().equals(sender.getName())){
                    Handler.desc.remove(dc);
                    sender.addChatMessage(new TextComponentString("Logged in successfully."));
                    ((EntityPlayerMP)sender).setPositionAndUpdate(dc.getPos().getX(),dc.getPos().getY(),dc.getPos().getZ());
                    return;
                }
            }
        } else {
            sender.addChatMessage(new TextComponentString("Passwords don't match."));
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        return new ArrayList<String>();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return true;
    }

    @Override
    public int compareTo(ICommand iCommand) {
        return this.getCommandName().compareTo(iCommand.getCommandName());
    }

    public static String generateHash(String in) {
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(in);
    }
}
