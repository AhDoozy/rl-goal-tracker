package com.toofifty.goaltracker.utils;

import com.toofifty.goaltracker.models.task.QuestTask;
import com.toofifty.goaltracker.models.task.SkillLevelTask;
import com.toofifty.goaltracker.models.task.Task;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

import java.util.*;

/**
 * Static lookup of quest prerequisites (quests + skill levels) and a helper to
 * expand nested requirements with appropriate indent levels.
 */
public final class QuestRequirements
{
    // Map of quests to their requirements (quests and skills)
    private static final Map<Quest, List<Task>> REQUIREMENT_MAP;

    // Static initializer with minimal dependencies
    static {
        REQUIREMENT_MAP = new HashMap<>();
        REQUIREMENT_MAP.put(
                Quest.FAIRYTALE_I__GROWING_PAINS,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.LOST_CITY).build(),
                        QuestTask.builder().quest(Quest.NATURE_SPIRIT).build(),
                        SkillLevelTask.builder().skill(Skill.FARMING).level(18).build()
                )
        );
        REQUIREMENT_MAP.put(
                Quest.NATURE_SPIRIT,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.PRIEST_IN_PERIL).build(),
                        QuestTask.builder().quest(Quest.THE_RESTLESS_GHOST).build(),
                        SkillLevelTask.builder().skill(Skill.PRAYER).level(18).build()
                )
        );
        REQUIREMENT_MAP.put(
                Quest.DRAGON_SLAYER_I,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.ATTACK).level(32).build(),
                        QuestTask.builder().quest(Quest.PRIEST_IN_PERIL).build()
                )
        );
        // Jungle Potion requires Druidic Ritual and Herblore 3【532018769049742†L45-L49】.
        REQUIREMENT_MAP.put(
                Quest.JUNGLE_POTION,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.DRUIDIC_RITUAL).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(3).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.BIOHAZARD,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.PLAGUE_CITY).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.EAGLES_PEAK,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.HUNTER).level(27).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.ELEMENTAL_WORKSHOP_I,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.MINING).level(20).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(20).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(20).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.FISHING_CONTEST,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.FISHING).level(10).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.OBSERVATORY_QUEST,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(10).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.RECRUITMENT_DRIVE,
                Collections.singletonList(
                        QuestTask.builder().quest(Quest.BLACK_KNIGHTS_FORTRESS).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.TOWER_OF_LIFE,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.CONSTRUCTION).level(10).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.ANIMAL_MAGNETISM,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.ERNEST_THE_CHICKEN).build(),
                        QuestTask.builder().quest(Quest.PRIEST_IN_PERIL).build(),
                        QuestTask.builder().quest(Quest.THE_RESTLESS_GHOST).build(),
                        SkillLevelTask.builder().skill(Skill.SLAYER).level(18).build(),
                        SkillLevelTask.builder().skill(Skill.RANGED).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(19).build(),
                        SkillLevelTask.builder().skill(Skill.WOODCUTTING).level(35).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.ANOTHER_SLICE_OF_HAM,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.DEATH_TO_THE_DORGESHUUN).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.BIG_CHOMPY_BIRD_HUNTING,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.COOKING).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.FLETCHING).level(5).build(),
                        SkillLevelTask.builder().skill(Skill.RANGED).level(30).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.COLD_WAR,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.CONSTRUCTION).level(34).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.HUNTER).level(10).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(15).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.CREATURE_OF_FENKENSTRAIN,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(20).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(25).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.DARKNESS_OF_HALLOWVALE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.IN_AID_OF_THE_MYREQUE).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(26).build(),
                        SkillLevelTask.builder().skill(Skill.CONSTRUCTION).level(5).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(32).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(33).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(20).build(),
                        SkillLevelTask.builder().skill(Skill.STRENGTH).level(40).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(22).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.DEATH_TO_THE_DORGESHUUN,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_LOST_TRIBE).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(23).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(17).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(23).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_DIG_SITE,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(10).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(10).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(25).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.ELEMENTAL_WORKSHOP_II,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.ELEMENTAL_WORKSHOP_I).build(),
                        SkillLevelTask.builder().skill(Skill.DEFENCE).level(20).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(20).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(20).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(30).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.ENLIGHTENED_JOURNEY,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(36).build(),
                        SkillLevelTask.builder().skill(Skill.FIREMAKING).level(20).build(),
                        SkillLevelTask.builder().skill(Skill.FARMING).level(30).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_EYES_OF_GLOUPHRIE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_GRAND_TREE).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(46).build(),
                        SkillLevelTask.builder().skill(Skill.FARMING).level(5).build(),
                        SkillLevelTask.builder().skill(Skill.CONSTRUCTION).level(5).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_FEUD,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(30).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.FORGETTABLE_TALE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_GIANT_DWARF).build(),
                        SkillLevelTask.builder().skill(Skill.COOKING).level(22).build(),
                        SkillLevelTask.builder().skill(Skill.FARMING).level(12).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(17).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(16).build(),
                        SkillLevelTask.builder().skill(Skill.FISHING).level(10).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(33).build(),
                        SkillLevelTask.builder().skill(Skill.STRENGTH).level(14).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.GARDEN_OF_TRANQUILLITY,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.CREATURE_OF_FENKENSTRAIN).build(),
                        SkillLevelTask.builder().skill(Skill.FARMING).level(25).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(20).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(25).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.GHOSTS_AHOY,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.PRIEST_IN_PERIL).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(25).build(),
                        SkillLevelTask.builder().skill(Skill.COOKING).level(20).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_GIANT_DWARF,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(12).build(),
                        SkillLevelTask.builder().skill(Skill.FIREMAKING).level(16).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(33).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(14).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_GOLEM,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(20).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(25).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_HAND_IN_THE_SAND,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(49).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(17).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.HOLY_GRAIL,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.MERLINS_CRYSTAL).build(),
                        SkillLevelTask.builder().skill(Skill.ATTACK).level(20).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.ICTHLARINS_LITTLE_HELPER,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.PRINCE_ALI_RESCUE).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.IN_AID_OF_THE_MYREQUE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.IN_SEARCH_OF_THE_MYREQUE).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(25).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(25).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(7).build(),
                        SkillLevelTask.builder().skill(Skill.FISHING).level(15).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.IN_SEARCH_OF_THE_MYREQUE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.NATURE_SPIRIT).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(25).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_KNIGHTS_SWORD,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.MINING).level(10).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_LOST_TRIBE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.GOBLIN_DIPLOMACY).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(13).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(17).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(13).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.MAKING_HISTORY,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.PRIEST_IN_PERIL).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.MOUNTAIN_DAUGHTER,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(20).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.MY_ARMS_BIG_ADVENTURE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.JUNGLE_POTION).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(15).build(),
                        SkillLevelTask.builder().skill(Skill.FARMING).level(29).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(31).build(),
                        SkillLevelTask.builder().skill(Skill.WOODCUTTING).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.COOKING).level(10).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.OLAFS_QUEST,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(40).build(),
                        SkillLevelTask.builder().skill(Skill.FIREMAKING).level(50).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.RATCATCHERS,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.ICTHLARINS_LITTLE_HELPER).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(12).build(),
                        SkillLevelTask.builder().skill(Skill.SLAYER).level(16).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(33).build(),
                        SkillLevelTask.builder().skill(Skill.HUNTER).level(14).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.SCORPION_CATCHER,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.ALFRED_GRIMHANDS_BARCRAWL).build(),
                        SkillLevelTask.builder().skill(Skill.PRAYER).level(31).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.SEA_SLUG,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.FIREMAKING).level(30).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.SHADES_OF_MORTTON,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(20).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(5).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(15).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.SHADOW_OF_THE_STORM,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.DEMON_SLAYER).build(),
                        QuestTask.builder().quest(Quest.THE_GOLEM).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(25).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_SLUG_MENACE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.WANTED).build(),
                        SkillLevelTask.builder().skill(Skill.RUNECRAFT).level(13).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.SLAYER).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(17).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(30).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.SPIRITS_OF_THE_ELID,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(33).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(37).build(),
                        SkillLevelTask.builder().skill(Skill.RANGED).level(37).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(37).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.TAI_BWO_WANNAI_TRIO,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(15).build(),
                        SkillLevelTask.builder().skill(Skill.COOKING).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(5).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.A_TAIL_OF_TWO_CATS,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.ICTHLARINS_LITTLE_HELPER).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.TEARS_OF_GUTHIX,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(20).build(),
                        SkillLevelTask.builder().skill(Skill.FIREMAKING).level(49).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(20).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_TOURIST_TRAP,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.FLETCHING).level(10).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(20).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.TRIBAL_TOTEM,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(21).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.WANTED,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.RECRUITMENT_DRIVE).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(13).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(17).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(13).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.WATCHTOWER,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(25).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(14).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(15).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(40).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(15).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.WHAT_LIES_BELOW,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.RUNE_MYSTERIES).build(),
                        SkillLevelTask.builder().skill(Skill.RUNECRAFT).level(35).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.ZOGRE_FLESH_EATERS,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.JUNGLE_POTION).build(),
                        QuestTask.builder().quest(Quest.BIG_CHOMPY_BIRD_HUNTING).build(),
                        SkillLevelTask.builder().skill(Skill.FLETCHING).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(8).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(4).build(),
                        SkillLevelTask.builder().skill(Skill.RANGED).level(30).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.BETWEEN_A_ROCK,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.DWARF_CANNON).build(),
                        SkillLevelTask.builder().skill(Skill.DEFENCE).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(10).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(40).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(50).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.CABIN_FEVER,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.RUM_DEAL).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(42).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(45).build(),
                        SkillLevelTask.builder().skill(Skill.RANGED).level(40).build(),
                        SkillLevelTask.builder().skill(Skill.COOKING).level(50).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.DEVIOUS_MINDS,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.WANTED).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.RUNECRAFT).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(65).build(),
                        SkillLevelTask.builder().skill(Skill.FLETCHING).level(50).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.EADGARS_RUSE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.DRUIDIC_RITUAL).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(31).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.ENAKHRAS_LAMENT,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.FIREMAKING).level(45).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(39).build(),
                        SkillLevelTask.builder().skill(Skill.PRAYER).level(43).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.FAIRYTALE_II__CURE_A_QUEEN,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.FAIRYTALE_I__GROWING_PAINS).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(40).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(49).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(57).build(),
                        SkillLevelTask.builder().skill(Skill.FARMING).level(37).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.FAMILY_CREST,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.MINING).level(40).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(59).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(40).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(40).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_FREMENNIK_ISLES,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_FREMENNIK_TRIALS).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(40).build(),
                        SkillLevelTask.builder().skill(Skill.CONSTRUCTION).level(20).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_GRAND_TREE,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(25).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_GREAT_BRAIN_ROBBERY,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.CABIN_FEVER).build(),
                        SkillLevelTask.builder().skill(Skill.PRAYER).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.CONSTRUCTION).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.COOKING).level(31).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(45).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.HAUNTED_MINE,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(15).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(35).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.HEROES_QUEST,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.SHIELD_OF_ARRAV).build(),
                        QuestTask.builder().quest(Quest.DRAGON_SLAYER_I).build(),
                        QuestTask.builder().quest(Quest.MERLINS_CRYSTAL).build(),
                        QuestTask.builder().quest(Quest.LOST_CITY).build(),
                        SkillLevelTask.builder().skill(Skill.COOKING).level(53).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(25).build(),
                        SkillLevelTask.builder().skill(Skill.FISHING).level(53).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(25).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(50).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.HORROR_FROM_THE_DEEP,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(35).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.KINGS_RANSOM,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.BLACK_KNIGHTS_FORTRESS).build(),
                        QuestTask.builder().quest(Quest.HOLY_GRAIL).build(),
                        QuestTask.builder().quest(Quest.MURDER_MYSTERY).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(65).build(),
                        SkillLevelTask.builder().skill(Skill.DEFENCE).level(45).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(18).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(45).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.LOST_CITY,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(31).build(),
                        SkillLevelTask.builder().skill(Skill.WOODCUTTING).level(36).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.LUNAR_DIPLOMACY,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_FREMENNIK_TRIALS).build(),
                        SkillLevelTask.builder().skill(Skill.WOODCUTTING).level(55).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(60).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(5).build(),
                        SkillLevelTask.builder().skill(Skill.FIREMAKING).level(49).build(),
                        SkillLevelTask.builder().skill(Skill.DEFENCE).level(40).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(61).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(32).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.ONE_SMALL_FAVOUR,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(36).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(25).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(18).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(30).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.ROYAL_TROUBLE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THRONE_OF_MISCELLANIA).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(40).build(),
                        SkillLevelTask.builder().skill(Skill.SLAYER).level(40).build(),
                        SkillLevelTask.builder().skill(Skill.FARMING).level(53).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.RUM_DEAL,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.ZOGRE_FLESH_EATERS).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.PRAYER).level(47).build(),
                        SkillLevelTask.builder().skill(Skill.FISHING).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.FARMING).level(40).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.SHILO_VILLAGE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.JUNGLE_POTION).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(32).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(20).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.TEMPLE_OF_IKOV,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.RANGED).level(40).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(42).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THRONE_OF_MISCELLANIA,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.HEROES_QUEST).build(),
                        SkillLevelTask.builder().skill(Skill.COOKING).level(45).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(40).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.TROLL_ROMANCE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.TROLL_STRONGHOLD).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(28).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.TROLL_STRONGHOLD,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.DEATH_PLATEAU).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(15).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.UNDERGROUND_PASS,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.BIOHAZARD).build(),
                        SkillLevelTask.builder().skill(Skill.RANGED).level(25).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.CONTACT,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.PRINCE_ALI_RESCUE).build(),
                        QuestTask.builder().quest(Quest.ICTHLARINS_LITTLE_HELPER).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.DESERT_TREASURE_I,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.TEMPLE_OF_IKOV).build(),
                        QuestTask.builder().quest(Quest.THE_DIG_SITE).build(),
                        QuestTask.builder().quest(Quest.TROLL_STRONGHOLD).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(53).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.SLAYER).level(10).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(10).build(),
                        SkillLevelTask.builder().skill(Skill.FIREMAKING).level(50).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.DREAM_MENTOR,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.LUNAR_DIPLOMACY).build(),
                        SkillLevelTask.builder().skill(Skill.DEFENCE).level(85).build(),
                        SkillLevelTask.builder().skill(Skill.HITPOINTS).level(65).build(),
                        SkillLevelTask.builder().skill(Skill.WOODCUTTING).level(55).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(60).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(45).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(40).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(49).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.GRIM_TALES,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(59).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(52).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(45).build(),
                        SkillLevelTask.builder().skill(Skill.FISHING).level(58).build(),
                        SkillLevelTask.builder().skill(Skill.WOODCUTTING).level(71).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.LEGENDS_QUEST,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.FAMILY_CREST).build(),
                        QuestTask.builder().quest(Quest.SHILO_VILLAGE).build(),
                        QuestTask.builder().quest(Quest.HEROES_QUEST).build(),
                        QuestTask.builder().quest(Quest.UNDERGROUND_PASS).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.FISHING).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(52).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.STRENGTH).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.PRAYER).level(42).build(),
                        SkillLevelTask.builder().skill(Skill.WOODCUTTING).level(50).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.MONKEY_MADNESS_I,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_GRAND_TREE).build(),
                        QuestTask.builder().quest(Quest.TREE_GNOME_VILLAGE).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.MOURNINGS_END_PART_I,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.ROVING_ELVES).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(60).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(50).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.MOURNINGS_END_PART_II,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.MOURNINGS_END_PART_I).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(60).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.RECIPE_FOR_DISASTER,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.COOKS_ASSISTANT).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.REGICIDE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.UNDERGROUND_PASS).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(56).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(10).build(),
                        SkillLevelTask.builder().skill(Skill.FLETCHING).level(25).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.ROVING_ELVES,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.REGICIDE).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(56).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(10).build(),
                        SkillLevelTask.builder().skill(Skill.RANGED).level(25).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.SWAN_SONG,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.ONE_SMALL_FAVOUR).build(),
                        SkillLevelTask.builder().skill(Skill.WOODCUTTING).level(62).build(),
                        SkillLevelTask.builder().skill(Skill.COOKING).level(66).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(42).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(45).build(),
                        SkillLevelTask.builder().skill(Skill.FISHING).level(62).build(),
                        SkillLevelTask.builder().skill(Skill.SLAYER).level(18).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.DRAGON_SLAYER_II,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.LEGENDS_QUEST).build(),
                        QuestTask.builder().quest(Quest.A_TAIL_OF_TWO_CATS).build(),
                        QuestTask.builder().quest(Quest.DREAM_MENTOR).build(),
                        QuestTask.builder().quest(Quest.BONE_VOYAGE).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(60).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(60).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(68).build(),
                        SkillLevelTask.builder().skill(Skill.HUNTER).level(60).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(60).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(75).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.MONKEY_MADNESS_II,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.ENLIGHTENED_JOURNEY).build(),
                        QuestTask.builder().quest(Quest.THE_EYES_OF_GLOUPHRIE).build(),
                        QuestTask.builder().quest(Quest.THE_GRAND_TREE).build(),
                        SkillLevelTask.builder().skill(Skill.SLAYER).level(69).build(),
                        SkillLevelTask.builder().skill(Skill.HUNTER).level(60).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(55).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(70).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(70).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.SONG_OF_THE_ELVES,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.MOURNINGS_END_PART_II).build(),
                        QuestTask.builder().quest(Quest.MAKING_HISTORY).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(70).build(),
                        SkillLevelTask.builder().skill(Skill.CONSTRUCTION).level(70).build(),
                        SkillLevelTask.builder().skill(Skill.FARMING).level(70).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(70).build(),
                        SkillLevelTask.builder().skill(Skill.HUNTER).level(70).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(70).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(70).build(),
                        SkillLevelTask.builder().skill(Skill.WOODCUTTING).level(70).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.SINS_OF_THE_FATHER,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.A_TASTE_OF_HOPE).build(),
                        SkillLevelTask.builder().skill(Skill.WOODCUTTING).level(62).build(),
                        SkillLevelTask.builder().skill(Skill.FLETCHING).level(60).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(56).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(52).build(),
                        SkillLevelTask.builder().skill(Skill.ATTACK).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.SLAYER).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(49).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.A_TASTE_OF_HOPE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.DARKNESS_OF_HALLOWVALE).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.A_KINGDOM_DIVIDED,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.CLIENT_OF_KOUREND).build(),
                        QuestTask.builder().quest(Quest.X_MARKS_THE_SPOT).build(),
                        QuestTask.builder().quest(Quest.THE_DEPTHS_OF_DESPAIR).build(),
                        QuestTask.builder().quest(Quest.THE_QUEEN_OF_THIEVES).build(),
                        QuestTask.builder().quest(Quest.THE_ASCENT_OF_ARCEUUS).build(),
                        QuestTask.builder().quest(Quest.THE_FORSAKEN_TOWER).build(),
                        QuestTask.builder().quest(Quest.TALE_OF_THE_RIGHTEOUS).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(54).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(52).build(),
                        SkillLevelTask.builder().skill(Skill.WOODCUTTING).level(52).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(50).build(),
                        SkillLevelTask.builder().skill(Skill.MINING).level(42).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(38).build(),
                        SkillLevelTask.builder().skill(Skill.MAGIC).level(35).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.A_NIGHT_AT_THE_THEATRE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.A_TASTE_OF_HOPE).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_FREMENNIK_EXILES,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_FREMENNIK_ISLES).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(65).build(),
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(60).build(),
                        SkillLevelTask.builder().skill(Skill.SLAYER).level(60).build(),
                        SkillLevelTask.builder().skill(Skill.FISHING).level(60).build(),
                        SkillLevelTask.builder().skill(Skill.RUNECRAFT).level(55).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.BONE_VOYAGE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_DIG_SITE).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.SLEEPING_GIANTS,
                Arrays.asList(
                        SkillLevelTask.builder().skill(Skill.SMITHING).level(15).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.TEMPLE_OF_THE_EYE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.RUNE_MYSTERIES).build(),
                        SkillLevelTask.builder().skill(Skill.RUNECRAFT).level(10).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.LAND_OF_THE_GOBLINS,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.ANOTHER_SLICE_OF_HAM).build(),
                        SkillLevelTask.builder().skill(Skill.PRAYER).level(30).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(36).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(36).build(),
                        SkillLevelTask.builder().skill(Skill.HERBLORE).level(37).build(),
                        SkillLevelTask.builder().skill(Skill.FISHING).level(36).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.BENEATH_CURSED_SANDS,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.CONTACT).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(62).build(),
                        SkillLevelTask.builder().skill(Skill.CRAFTING).level(55).build(),
                        SkillLevelTask.builder().skill(Skill.FIREMAKING).level(55).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.SECRETS_OF_THE_NORTH,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.MAKING_HISTORY).build(),
                        QuestTask.builder().quest(Quest.HAZEEL_CULT).build(),
                        SkillLevelTask.builder().skill(Skill.AGILITY).level(69).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(64).build(),
                        SkillLevelTask.builder().skill(Skill.HUNTER).level(56).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.DESERT_TREASURE_II__THE_FALLEN_EMPIRE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.DESERT_TREASURE_I).build(),
                        QuestTask.builder().quest(Quest.SECRETS_OF_THE_NORTH).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.TWILIGHTS_PROMISE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.CHILDREN_OF_THE_SUN).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.AT_FIRST_LIGHT,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.TWILIGHTS_PROMISE).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.PERILOUS_MOONS,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.TWILIGHTS_PROMISE).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_RIBBITING_TALE_OF_A_LILY_PAD_LABOUR_DISPUTE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.TWILIGHTS_PROMISE).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_HEART_OF_DARKNESS,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.TWILIGHTS_PROMISE).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.DEATH_ON_THE_ISLE,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.TWILIGHTS_PROMISE).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.MEAT_AND_GREET,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.TWILIGHTS_PROMISE).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.ETHICALLY_ACQUIRED_ANTIQUITIES,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.TWILIGHTS_PROMISE).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.DEFENDER_OF_VARROCK,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.SHIELD_OF_ARRAV).build(),
                        QuestTask.builder().quest(Quest.ROMEO__JULIET).build(),
                        QuestTask.builder().quest(Quest.THE_KNIGHTS_SWORD).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_CURSE_OF_ARRAV,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.DEFENDER_OF_VARROCK).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.WHILE_GUTHIX_SLEEPS,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_CURSE_OF_ARRAV).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.THE_FINAL_DAWN,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_HEART_OF_DARKNESS).build(),
                        QuestTask.builder().quest(Quest.PERILOUS_MOONS).build(),
                        SkillLevelTask.builder().skill(Skill.THIEVING).level(66).build(),
                        SkillLevelTask.builder().skill(Skill.FLETCHING).level(52).build(),
                        SkillLevelTask.builder().skill(Skill.RUNECRAFT).level(52).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.SHADOWS_OF_CUSTODIA,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_FINAL_DAWN).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.SCRAMBLED,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_FINAL_DAWN).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.AN_EXISTENTIAL_CRISIS,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_FINAL_DAWN).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.IMPENDING_CHAOS,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_FINAL_DAWN).build()
                )
        );

        REQUIREMENT_MAP.put(
                Quest.VALE_TOTEMS,
                Arrays.asList(
                        QuestTask.builder().quest(Quest.THE_FINAL_DAWN).build()
                )
        );
    }

    /**
     * Get all requirements for a quest, including recursive sub-quest requirements.
     * @param quest The quest to get requirements for.
     * @param indentLevel The indentation level for the tasks (0 for main quest).
     * @return List of tasks (quest and skill requirements) with appropriate indentation.
     */
    public static List<Task> getRequirements(Quest quest, int indentLevel)
    {
        List<Task> tasks = new ArrayList<>();
        if (indentLevel >= 3) {
            // Respect Task.java's max indent level of 3
            return tasks;
        }

        List<Task> directRequirements = REQUIREMENT_MAP.getOrDefault(quest, Collections.emptyList());
        for (Task task : directRequirements) {
            Task withIndent = copyWithIndent(task, indentLevel + 1);
            tasks.add(withIndent);

            if (withIndent instanceof QuestTask) {
                Quest subQuest = ((QuestTask) withIndent).getQuest();
                List<Task> subRequirements = getRequirements(subQuest, indentLevel + 1);
                tasks.addAll(subRequirements);
            }
        }

        return tasks;
    }

    private static Task copyWithIndent(Task task, int indentLevel)
    {
        if (task instanceof QuestTask) {
            QuestTask qt = (QuestTask) task;
            return QuestTask.builder()
                    .quest(qt.getQuest())
                    .indentLevel(indentLevel)
                    .build();
        }
        if (task instanceof SkillLevelTask) {
            SkillLevelTask st = (SkillLevelTask) task;
            return SkillLevelTask.builder()
                    .skill(st.getSkill())
                    .level(st.getLevel())
                    .indentLevel(indentLevel)
                    .build();
        }
        // Fallback: set indent on the same instance (least preferable, but safe for unknown subclasses)
        task.setIndentLevel(indentLevel);
        return task;
    }
}