package dev.command;

import com.google.gson.Gson;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class List {

    private static final Gson GSON = new Gson();

    public static void execute(String args) {
        java.util.List<EmiStack> stacks = EmiApi.getIndexStacks();

        java.util.List<String> names = stacks.stream()
                .filter(s -> !s.isEmpty())
                .map(s -> s.getId() != null ? s.getId().toString() : null)
                .filter(id -> id != null)
                .filter(id -> args.isEmpty() || id.contains(args))
                .distinct()
                .toList();

        System.out.println(GSON.toJson(names));
        System.out.flush();
    }
}