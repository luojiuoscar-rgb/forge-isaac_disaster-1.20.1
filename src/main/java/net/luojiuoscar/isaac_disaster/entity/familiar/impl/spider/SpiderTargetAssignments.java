package net.luojiuoscar.isaac_disaster.entity.familiar.impl.spider;

import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/** Each enemy recruits nearby spiders; priority is computed once before entering this pass. */
final class SpiderTargetAssignments<T> {
    record Spider(int id, Vec3 position) { }
    record Target<T>(T value, int id, int priority, Vec3 position) { }
    record Cell(int x, int y, int z) { }
    private record Choice<T>(Target<T> target, double distance) { }

    private final Map<Integer, Choice<T>> choices = new HashMap<>();

    void rebuild(List<Spider> spiders, List<Target<T>> targets) {
        choices.clear();
        Map<Cell, List<Spider>> cells = new HashMap<>();
        for (Spider spider : spiders) {
            Vec3 p = spider.position();
            cells.computeIfAbsent(new Cell(cell(p.x), cell(p.y), cell(p.z)), ignored -> new ArrayList<>()).add(spider);
        }
        for (Target<T> target : targets) {
            Vec3 p = target.position();
            for (int x = cell(p.x - 8); x <= cell(p.x + 8); x++) {
                for (int y = cell(p.y - 8); y <= cell(p.y + 8); y++) {
                    for (int z = cell(p.z - 8); z <= cell(p.z + 8); z++) {
                        List<Spider> neighbors = cells.get(new Cell(x, y, z));
                        if (neighbors == null) continue;
                        for (Spider spider : neighbors) {
                            double distance = p.distanceToSqr(spider.position());
                            if (distance > 64) continue;
                            Choice<T> old = choices.get(spider.id());
                            if (old == null || target.priority() < old.target().priority()
                                    || target.priority() == old.target().priority() && (distance < old.distance()
                                    || distance == old.distance() && target.id() < old.target().id())) {
                                choices.put(spider.id(), new Choice<>(target, distance));
                            }
                        }
                    }
                }
            }
        }
    }

    T target(int spiderId) {
        Choice<T> choice = choices.get(spiderId);
        return choice == null ? null : choice.target().value();
    }

    void remove(int spiderId) { choices.remove(spiderId); }

    static int cell(double coordinate) { return (int) Math.floor(coordinate / 4); }
}
