package dev.command;

import com.google.gson.Gson;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.runtime.Channel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class Recipe {

    private static final Gson GSON = new Gson();

    public static void execute(String args, Channel channel) {
        if (args.isEmpty()) {
            channel.writeLine("{\"error\":\"Usage: /recipe <item_id> [uses]\"}");
            return;
        }

        String[] parts = args.split(" ", 2);
        String itemId = parts[0];
        boolean uses = parts.length > 1 && parts[1].equalsIgnoreCase("uses");

        ResourceLocation loc;
        try {
            loc = new ResourceLocation(itemId);
        } catch (Exception e) {
            channel.writeLine("{\"error\":\"Invalid resource location: " + itemId + "\"}");
            return;
        }

        EmiStack target = EmiApi.getIndexStacks().stream()
                .filter(s -> loc.equals(s.getId()))
                .findFirst()
                .orElse(null);

        if (target == null) {
            channel.writeLine("{\"error\":\"Item not found in EMI index: " + itemId + "\"}");
            return;
        }

        List<EmiRecipe> recipes;
        if (uses) {
            recipes = EmiApi.getRecipeManager().getRecipesByInput(target);
        } else {
            recipes = EmiApi.getRecipeManager().getRecipesByOutput(target);
        }

        List<String> ids = recipes.stream()
                .filter(r -> r.getId() != null)
                .map(r -> r.getId().toString())
                .toList();

        channel.writeLine(GSON.toJson(ids));
    }
}