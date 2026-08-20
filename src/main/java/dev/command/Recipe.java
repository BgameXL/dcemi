package dev.command;

import com.google.gson.Gson;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class Recipe {

    private static final Gson GSON = new Gson();

    public static void execute(String args) {
        if (args.isEmpty()) {
            System.out.println("{\"error\":\"Usage: /recipe <item_id> [uses]\"}");
            System.out.flush();
            return;
        }

        String[] parts = args.split(" ", 2);
        String itemId = parts[0];
        boolean uses = parts.length > 1 && parts[1].equalsIgnoreCase("uses");

        ResourceLocation loc;
        try {
            loc = new ResourceLocation(itemId);
        } catch (Exception e) {
            System.out.println("{\"error\":\"Invalid resource location: " + itemId + "\"}");
            System.out.flush();
            return;
        }

        EmiStack target = EmiApi.getIndexStacks().stream()
                .filter(s -> loc.equals(s.getId()))
                .findFirst()
                .orElse(null);

        if (target == null) {
            System.out.println("{\"error\":\"Item not found in EMI index: " + itemId + "\"}");
            System.out.flush();
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

        System.out.println(GSON.toJson(ids));
        System.out.flush();
    }
}