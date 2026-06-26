package io.github.pixeldungeonmultiplayer.shattered.server.network.actions.serializers;

import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedString;
import io.github.pixeldungeonmultiplayer.common.localizedstring.LocalizedKey;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.PowerOfMany;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.SpiritHawk;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.ShadowClone;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.SmokeBomb;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalSpire;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.EnergyCrystal;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import io.github.pixeldungeonmultiplayer.shattered.server.network.jsondiff.JSONObjectDiff;
import io.github.pixeldungeonmultiplayer.shattered.server.network.actions.JournalSnapshotAction;
import io.github.pixeldungeonmultiplayer.shattered.server.network.serializers.SerializationContext;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.sprites.EarthGuardianSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WardSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickRecipe;
import com.watabou.utils.Reflection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JournalSnapshotActionSerializer extends NetworkActionSerializer<JournalSnapshotAction> {

	private static final LocalizedString UNKNOWN = LocalizedString.raw("???");
	private static final String GUIDE = "com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal$GuideTab";
	private static final String NOTES = "com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal$NotesTab";
	private static final String CATALOG = "com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal$CatalogTab";
	private static final String BADGES = "com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal$BadgesTab";

	private static final java.util.WeakHashMap<com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero, JSONObject> lastSnapshots =
			new java.util.WeakHashMap<>();

	@Override
	protected @Nullable JSONObject serializeInternal(@NotNull JournalSnapshotAction obj, @NotNull SerializationContext ctx, @NotNull String profile) {
		JSONObject root = new JSONObject();
		JSONArray tabs = new JSONArray();
		tabs.put(notesTab());
		tabs.put(guideTab());
		tabs.put(alchemyTab(ctx));
		tabs.put(catalogTab());
		tabs.put(badgesTab());
		applyAfterField(tabs);
		root.put("tabs", tabs);

		if (ctx.observer != null) {
			JSONObject lastSnapshot;
			synchronized (lastSnapshots) {
				lastSnapshot = obj.forceFull ? null : lastSnapshots.get(ctx.observer);
			}
			if (lastSnapshot == null) {
				lastSnapshot = new JSONObject();
			}
			JSONObject patch = JSONObjectDiff.diff(lastSnapshot, root);
			if (patch == null || patch.length() == 0) {
				return null;
			} // no diff
			synchronized (lastSnapshots) {
				lastSnapshots.put(ctx.observer, root);
			}
			return patch;
		}

		return root;
	}

	private static JSONObject notesTab() {
		JSONObject tab = tab("notes", msg(NOTES, "title"), icon("JOURNAL"));
		JSONArray entries = new JSONArray();
		entries.put(header(LocalizedString.concat("_", msg(NOTES, "title"), "_"), 9, true));
		entries.put(header(msg(NOTES, "desc"), 6, true));

		ArrayList<Notes.CustomRecord> customRecs = Notes.getRecords(Notes.CustomRecord.class);
		if (!customRecs.isEmpty()) {
			entries.put(header(LocalizedString.concat("_", msg(NOTES, "custom_notes"), "_ (", customRecs.size(), "/", Notes.customRecordLimit(), ")")));
			for (Notes.CustomRecord rec : customRecs) {
				entries.put(noteEntry(rec));
			}
		}

		for (int depth = Statistics.deepestFloor; depth > 0; depth--) {
			LocalizedString floor = msg(NOTES, "floor_header", depth);
			entries.put(header(depth == Dungeon.depth ? LocalizedString.concat("_", floor, "_") : floor));
			for (Notes.Record rec : Notes.getRecords(depth)) {
				entries.put(noteEntry(rec));
			}
		}

		applyAfterField(entries);
		tab.put("entries", entries);
		return tab;
	}

	private static JSONObject guideTab() {
		JSONObject tab = tab("guide", Document.ADVENTURERS_GUIDE.title(), itemIcon(ItemSpriteSheet.MASTERY));
		JSONArray entries = new JSONArray();
		entries.put(header(Document.ADVENTURERS_GUIDE.title()));
		for (String page : Document.ADVENTURERS_GUIDE.pageNames()) {
			boolean found = Document.ADVENTURERS_GUIDE.isPageFound(page);
			JSONObject entry = entry(page, "story", found ? Messages.titleCase(Document.ADVENTURERS_GUIDE.pageTitle(page)) : Messages.titleCase(msg(GUIDE, "missing")), Document.ADVENTURERS_GUIDE.pageBody(page), documentPageIcon(Document.ADVENTURERS_GUIDE, page, found));
			entry.put("enabled", found);
			entry.put("seen", found);
			entries.put(entry);
		}
		applyAfterField(entries);
		tab.put("entries", entries);
		return tab;
	}

	private static JSONObject serializeRecipe(QuickRecipe recipe, SerializationContext ctx) {
		JSONObject obj = new JSONObject();
		JSONArray ingredientsArr = new JSONArray();
		if (recipe.getIngredients() != null) {
			for (Item item : recipe.getIngredients()) {
				ingredientsArr.put(ctx.serialize(QuickRecipe.anonymize(item), "inventory"));
			}
		}
		obj.put("ingredients", ingredientsArr);
		obj.put("output", recipe.outputItem != null ? ctx.serialize(recipe.outputItem, "inventory") : JSONObject.NULL);
		obj.put("cost", recipe.energyCost);
		return obj;
	}

	private static JSONObject alchemyTab(SerializationContext ctx) {
		JSONObject tab = tab("alchemy", Document.ALCHEMY_GUIDE.title(), icon("ALCHEMY"));
		JSONArray entries = new JSONArray();
		int[] sprites = {
				ItemSpriteSheet.SEED_HOLDER,
				ItemSpriteSheet.STONE_HOLDER,
				ItemSpriteSheet.FOOD_HOLDER,
				ItemSpriteSheet.POTION_HOLDER,
				ItemSpriteSheet.SCROLL_HOLDER,
				ItemSpriteSheet.BOMB_HOLDER,
				ItemSpriteSheet.MISSILE_HOLDER,
				ItemSpriteSheet.ELIXIR_HOLDER,
				ItemSpriteSheet.SPELL_HOLDER
		};
		int i = 0;
		for (String page : Document.ALCHEMY_GUIDE.pageNames()) {
			boolean found = Document.ALCHEMY_GUIDE.isPageFound(page);
			JSONObject entry = entry(page, "page", Document.ALCHEMY_GUIDE.pageTitle(page), Document.ALCHEMY_GUIDE.pageBody(page), itemIcon(found ? sprites[i] : ItemSpriteSheet.SOMETHING));
			entry.put("title_icon", itemIcon(ItemSpriteSheet.ALCH_PAGE));
			entry.put("enabled", found);
			entry.put("seen", found);
			entry.put("read", Document.ALCHEMY_GUIDE.isPageRead(page));

			JSONArray recipesArr = new JSONArray();
			ArrayList<QuickRecipe> pageRecipes = QuickRecipe.getRecipes(i, ctx.observer);
			if (pageRecipes != null) {
				for (QuickRecipe r : pageRecipes) {
					if (r == null) {
						recipesArr.put(JSONObject.NULL);
					} else {
						recipesArr.put(serializeRecipe(r, ctx));
					}
				}
			}
			entry.put("recipes", recipesArr);

			entries.put(entry);
			i++;
		}
		applyAfterField(entries);
		tab.put("entries", entries);
		return tab;
	}

	private static JSONObject catalogTab() {
		JSONObject tab = tab("catalog", msg(CATALOG, "title"), icon("CATALOG"));
		JSONArray tabs = new JSONArray();
		tabs.put(catalogItemsTab("equipment", msg(CATALOG, "title_equipment"), itemIcon(ItemSpriteSheet.WEAPON_HOLDER), Catalog.equipmentCatalogs));
		tabs.put(catalogItemsTab("consumables", msg(CATALOG, "title_consumables"), itemIcon(ItemSpriteSheet.POTION_HOLDER), Catalog.consumableCatalogs));
		tabs.put(bestiaryTab());
		tabs.put(loreTab());
		applyAfterField(tabs);
		tab.put("tabs", tabs);
		return tab;
	}

	private static JSONObject catalogItemsTab(String id, LocalizedString title, JSONObject icon, List<Catalog> catalogs) {
		JSONObject tab = tab(id, title, icon);
		JSONArray entries = new JSONArray();
		int totalItems = 0;
		int totalSeen = 0;
		for (Catalog catalog : catalogs) {
			totalItems += catalog.totalItems();
			totalSeen += catalog.totalSeen();
		}
		entries.put(header(LocalizedString.concat("_", title, "_ (", totalSeen, "/", totalItems, ")"), 9, true));
		for (Catalog catalog : catalogs) {
			entries.put(header(LocalizedString.concat("_", Messages.titleCase(catalog.title()), "_ (", catalog.totalSeen(), "/", catalog.totalItems(), "):")));
			for (Class<?> itemClass : catalog.items()) {
				entries.put(itemEntry(itemClass));
			}
		}
		applyAfterField(entries);
		tab.put("entries", entries);
		return tab;
	}

	private static JSONObject bestiaryTab() {
		JSONObject tab = tab("bestiary", msg(CATALOG, "title_bestiary"), itemIcon(ItemSpriteSheet.MOB_HOLDER));
		JSONArray entries = new JSONArray();
		int totalItems = 0;
		int totalSeen = 0;
		for (Bestiary bestiary : Bestiary.values()) {
			totalItems += bestiary.totalEntities();
			totalSeen += bestiary.totalSeen();
		}
		entries.put(header(LocalizedString.concat("_", msg(CATALOG, "title_bestiary"), "_ (", totalSeen, "/", totalItems, ")"), 9, true));
		for (Bestiary bestiary : Bestiary.values()) {
			entries.put(header(LocalizedString.concat("_", Messages.titleCase(bestiary.title()), "_ (", bestiary.totalSeen(), "/", bestiary.totalEntities(), "):")));
			for (Class<?> entityClass : bestiary.entities()) {
				entries.put(entityEntry(entityClass));
			}
		}
		applyAfterField(entries);
		tab.put("entries", entries);
		return tab;
	}

	private static JSONObject loreTab() {
		JSONObject tab = tab("lore", msg(CATALOG, "title_lore"), itemIcon(ItemSpriteSheet.DOCUMENT_HOLDER));
		JSONArray entries = new JSONArray();
		int totalItems = 0;
		int totalSeen = 0;
		for (Document doc : Document.values()) {
			if (!doc.isLoreDoc()) continue;
			for (String page : doc.pageNames()) {
				totalItems++;
				if (doc.isPageFound(page)) totalSeen++;
			}
		}
		entries.put(header(LocalizedString.concat("_", msg(CATALOG, "title_lore"), "_ (", totalSeen, "/", totalItems, ")"), 9, true));
		for (Document doc : Document.values()) {
			if (!doc.isLoreDoc()) continue;
			int docItems = 0;
			int docSeen = 0;
			for (String page : doc.pageNames()) {
				docItems++;
				if (doc.isPageFound(page)) docSeen++;
			}
			entries.put(header(LocalizedString.concat(doc.anyPagesFound() ? LocalizedString.concat("_", Messages.titleCase(doc.title()), "_") : LocalizedString.raw("_???_"), " (", docSeen, "/", docItems, "):")));
			for (String page : doc.pageNames()) {
				boolean seen = doc.isPageFound(page);
				JSONObject entry = entry(page, seen ? "story" : "item", seen ? doc.pageTitle(page) : UNKNOWN, seen ? doc.pageBody(page) : LocalizedString.concat(msg(CATALOG, "not_seen_lore"), "\n\n", doc.discoverHint()), documentPageIcon(doc, page, seen));
				entry.put("seen", seen);
				entry.put("read", doc.isPageRead(page));
				if (seen) {
					entry.put("second_icon", textIcon(Integer.toString(doc.pageIdx(page) + 1)));
				}
				entries.put(entry);
			}
		}
		applyAfterField(entries);
		tab.put("entries", entries);
		return tab;
	}

	private static JSONObject badgesTab() {
		JSONObject tab = tab("badges", msg(BADGES, "title"), icon("BADGES"));
		JSONArray tabs = new JSONArray();
		tabs.put(badgeListTab("run_badges", msg(BADGES, "this_run"), false));
		tabs.put(badgeListTab("global_badges", msg(BADGES, "overall"), true));
		applyAfterField(tabs);
		tab.put("tabs", tabs);
		return tab;
	}

	private static JSONObject badgeListTab(String id, LocalizedString title, boolean global) {
		JSONObject tab = tab(id, title, icon("BADGES"));
		JSONArray entries = new JSONArray();
		for (Badges.Badge badge : Badges.filterReplacedBadges(global)) {
			if (badge.type == Badges.BadgeType.HIDDEN || badge.image < 0) continue;
			entries.put(badgeEntry(badge, true));
		}
		if (global) {
			ArrayList<Badges.Badge> locked = new ArrayList<>();
			for (Badges.Badge badge : Badges.Badge.values()) {
				if (badge.type != Badges.BadgeType.HIDDEN && badge.image >= 0 && !Badges.isUnlocked(badge)) {
					locked.add(badge);
				}
			}
			Badges.filterBadgesWithoutPrerequisites(locked);
			Collections.sort(locked);
			for (Badges.Badge badge : locked) {
				entries.put(badgeEntry(badge, false));
			}
		}
		applyAfterField(entries);
		tab.put("entries", entries);
		return tab;
	}

	private static String recordId(Notes.Record record) {
		if (record instanceof Notes.CustomRecord) {
			return "custom_" + ((Notes.CustomRecord) record).ID;
		} else if (record instanceof Notes.LandmarkRecord) {
			return "landmark_" + record.depth() + "_" + ((Notes.LandmarkRecord) record).landmark.name();
		} else if (record instanceof Notes.KeyRecord) {
			return "key_" + record.depth() + "_" + (((Notes.KeyRecord) record).key != null ? ((Notes.KeyRecord) record).key.getClass().getSimpleName() : "null");
		}
		return "record_" + record.hashCode();
	}

	private static JSONObject noteEntry(Notes.Record record) {
		JSONObject entry = entry(recordId(record), "item", Messages.titleCase(record.title()), record.desc(), noteIcon(record));
		putSecondIcon(entry, noteSecondIcon(record));
		return entry;
	}

	private static JSONObject itemEntry(Class<?> itemClass) {
		boolean seen = Catalog.isSeen(itemClass);
		JSONObject icon = itemIcon(ItemSpriteSheet.SOMETHING);
		LocalizedString title = UNKNOWN;
		LocalizedString desc = msg(CATALOG, "not_seen_item");

		try {
			if (Item.class.isAssignableFrom(itemClass)) {
				Item item = (Item) Reflection.newInstance(itemClass);
				if (seen) {
					if (item instanceof Ring) ((Ring) item).anonymize();
					if (item instanceof Potion) ((Potion) item).anonymize();
					if (item instanceof Scroll) ((Scroll) item).anonymize();
				}
				icon = itemIcon(!seen && item instanceof ExoticPotion ? ItemSpriteSheet.POTION_CRIMSON : item.image);
				icon.put("dark", !seen);
				if (seen) {
					title = Messages.titleCase(item.name());
					desc = item.info();
					int count = Catalog.useCount(itemClass);
					if (count > 1) {
						if (item.isUpgradable() || item instanceof Artifact) {
							desc = LocalizedString.concat(desc, "\n\n", msg(CATALOG, "upgrade_count", count));
						} else if (item instanceof Trinket) {
							desc = LocalizedString.concat(desc, "\n\n", msg(CATALOG, "trinket_count", count));
						} else if (item instanceof Gold) {
							desc = LocalizedString.concat(desc, "\n\n", msg(CATALOG, "gold_count", count));
						} else if (item instanceof EnergyCrystal) {
							desc = LocalizedString.concat(desc, "\n\n", msg(CATALOG, "energy_count", count));
						} else {
							desc = LocalizedString.concat(desc, "\n\n", msg(CATALOG, "use_count", count));
						}
					}
				} else {
					desc = LocalizedString.concat(desc, "\n\n", Messages.get(item, "discover_hint"));
				}
			} else if (Weapon.Enchantment.class.isAssignableFrom(itemClass)) {
				Weapon.Enchantment ench = (Weapon.Enchantment) Reflection.newInstance(itemClass);
				icon = itemIcon(ItemSpriteSheet.WORN_SHORTSWORD);
				icon.put("dark", !seen);
				title = seen ? Messages.titleCase(ench.name()) : UNKNOWN;
				desc = seen ? ench.desc() : LocalizedString.concat(msg(CATALOG, "not_seen_enchantment"), "\n\n", Messages.get(ench, "discover_hint"));
			} else if (Armor.Glyph.class.isAssignableFrom(itemClass)) {
				Armor.Glyph glyph = (Armor.Glyph) Reflection.newInstance(itemClass);
				icon = itemIcon(ItemSpriteSheet.ARMOR_CLOTH);
				icon.put("dark", !seen);
				title = seen ? Messages.titleCase(glyph.name()) : UNKNOWN;
				desc = seen ? glyph.desc() : LocalizedString.concat(msg(CATALOG, "not_seen_glyph"), "\n\n", Messages.get(glyph, "discover_hint"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONObject entry = entry(itemClass.getSimpleName(), "item", title, desc, icon);
		entry.put("seen", seen);
		return entry;
	}

	private static JSONObject entityEntry(Class<?> entityClass) {
		boolean seen = Bestiary.isSeen(entityClass);
		JSONObject icon = icon("WARNING");
		LocalizedString title = seen ? LocalizedString.EMPTY : UNKNOWN;
		LocalizedString desc = LocalizedString.EMPTY;
		try {
			if (Mob.class.isAssignableFrom(entityClass)) {
				icon = charIcon(entitySpriteName(entityClass));
				title = seen ? Messages.titleCase(Messages.get(entityClass, "name")) : UNKNOWN;
				if (seen) {
					desc = Messages.get(entityClass, "desc");
					if (Bestiary.encounterCount(entityClass) > 1) {
						desc = LocalizedString.concat(desc, "\n\n", msg(CATALOG, "enemy_count", Bestiary.encounterCount(entityClass)));
					}
				} else {
					icon.put("dark", true);
					if (entityClass == WandOfRegrowth.Lotus.class) {
						desc = msg(CATALOG, "not_seen_plant");
					} else if (Bestiary.ALLY.entities().contains(entityClass)) {
						desc = msg(CATALOG, "not_seen_ally");
					} else {
						desc = msg(CATALOG, "not_seen_enemy");
					}
					desc = LocalizedString.concat(desc, "\n\n", Messages.get(entityClass, "discover_hint"));
				}
			} else if (Trap.class.isAssignableFrom(entityClass)) {
				Trap trap = (Trap) Reflection.newInstance(entityClass);
				icon = terrainIcon((trap.active ? trap.color : Trap.BLACK) + trap.shape * 16);
				title = seen ? Messages.titleCase(trap.name()) : UNKNOWN;
				if (seen) {
					desc = trap.desc();
					if (Bestiary.encounterCount(entityClass) > 1) {
						desc = LocalizedString.concat(desc, "\n\n", msg(CATALOG, "trap_count", Bestiary.encounterCount(entityClass)));
					}
				} else {
					icon.put("dark", true);
					desc = LocalizedString.concat(msg(CATALOG, "not_seen_trap"), "\n\n", Messages.get(trap, "discover_hint"));
				}
			} else if (Plant.class.isAssignableFrom(entityClass)) {
				Plant plant = (Plant) Reflection.newInstance(entityClass);
				icon = terrainIcon(plant.image + 7 * 16);
				title = seen ? Messages.titleCase(plant.name()) : UNKNOWN;
				if (seen) {
					desc = plant.desc();
					if (Bestiary.encounterCount(entityClass) > 1) {
						desc = LocalizedString.concat(desc, "\n\n", msg(CATALOG, "plant_count", Bestiary.encounterCount(entityClass)));
					}
				} else {
					icon.put("dark", true);
					desc = LocalizedString.concat(msg(CATALOG, "not_seen_plant"), "\n\n", Messages.get(plant, "discover_hint"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONObject entry = entry(entityClass.getSimpleName(), "item", title, desc, icon);
		entry.put("seen", seen);
		return entry;
	}

	private static String entitySpriteName(Class<?> entityClass) {
		Class<?> spriteClass = fixedEntitySpriteClass(entityClass);
		if (spriteClass != null) {
			return spriteClass.getName();
		}
		try {
			Mob mob = (Mob) Reflection.newInstance(entityClass);
			if (mob instanceof Mimic || mob instanceof CrystalSpire) {
				mob.alignment = com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY;
			}
			if (mob instanceof WandOfWarding.Ward) {
				if (mob instanceof WandOfWarding.Ward.WardSentry) {
					for (int i = 0; i < 4; i++) ((WandOfWarding.Ward) mob).upgrade(3);
				} else {
					((WandOfWarding.Ward) mob).upgrade(0);
				}
			}
			return mob.spriteClass == null ? null : mob.spriteClass.getName();
		} catch (Exception ignored) {
			return null;
		}
	}

	private static @Nullable Class<?> fixedEntitySpriteClass(Class<?> entityClass) {
		if (entityClass == WandOfWarding.Ward.class || entityClass == WandOfWarding.Ward.WardSentry.class)
			return WardSprite.class;
		if (entityClass == com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth.EarthGuardian.class)
			return EarthGuardianSprite.class;
		if (entityClass == ShadowClone.ShadowAlly.class) return ShadowClone.ShadowSprite.class;
		if (entityClass == SmokeBomb.NinjaLog.class) return SmokeBomb.NinjaLogSprite.class;
		if (entityClass == SpiritHawk.HawkAlly.class) return SpiritHawk.HawkSprite.class;
		if (entityClass == PowerOfMany.LightAlly.class) return PowerOfMany.LightAllySprite.class;
		return null;
	}

	private static JSONObject badgeEntry(Badges.Badge badge, boolean unlocked) {
		JSONObject icon = badgeIcon(badge.image);
		icon.put("dark", !unlocked);
		LocalizedString desc = badge.desc();
		LocalizedString progress = Badges.showCompletionProgress(badge);
		if (progress != null) {
			desc = LocalizedString.concat(desc, progress);
		}
		JSONObject entry = entry(badge.name(), "badge", badge.title(), desc, icon);
		entry.put("seen", unlocked);
		return entry;
	}

	private static JSONObject noteIcon(Notes.Record record) {
		if (record instanceof Notes.KeyRecord) {
			Notes.KeyRecord key = (Notes.KeyRecord) record;
			return itemIcon(key.key.image);
		}
		if (record instanceof Notes.CustomRecord) {
			Notes.CustomRecord custom = (Notes.CustomRecord) record;
			if ((custom.type == Notes.CustomType.ITEM_TYPE || custom.type == Notes.CustomType.SPECIFIC_ITEM) && custom.itemClass != null) {
				try {
					Item item = (Item) Reflection.newInstance(custom.itemClass);
					return itemIcon(item.image);
				} catch (Exception ignored) {
				}
			}
			return icon(custom.type == Notes.CustomType.TEXT ? "SCROLL_COLOR" : "STAIRS");
		}
		return icon("STAIRS");
	}

	private static @Nullable JSONObject noteSecondIcon(Notes.Record record) {
		if (record instanceof Notes.KeyRecord) {
			Notes.KeyRecord key = (Notes.KeyRecord) record;
			return key.quantity() > 1 ? textIcon(Integer.toString(key.quantity())) : null;
		}
		if (record instanceof Notes.CustomRecord) {
			Notes.CustomRecord custom = (Notes.CustomRecord) record;
			if (custom.type == Notes.CustomType.DEPTH) {
				return textIcon(Integer.toString(custom.depth()));
			}
			if ((custom.type == Notes.CustomType.ITEM_TYPE || custom.type == Notes.CustomType.SPECIFIC_ITEM) && custom.itemClass != null) {
				try {
					Item item = (Item) Reflection.newInstance(custom.itemClass);
					return item.isIdentified() && item.icon != -1 ? itemIcon(item.icon) : null;
				} catch (Exception ignored) {
				}
			}
		}
		return null;
	}

	private static JSONObject documentPageIcon(Document doc, String page, boolean seen) {
		Icons pageIcon = doc.pageIcon(page);
		JSONObject icon = pageIcon == null ? itemIcon(doc.pageItemSprite(page)) : icon(pageIcon.name());
		icon.put("dark", !seen);
		return icon;
	}

	private static JSONObject tab(String id, LocalizedString title, JSONObject icon) {
		JSONObject tab = new JSONObject();
		tab.put("id", id);
		tab.put("title", title.toJsonObject());
		tab.put("icon", icon);
		return tab;
	}

	private static JSONObject header(LocalizedString title) {
		return header(title, 7, false);
	}

	private static JSONObject header(LocalizedString title, int size, boolean center) {
		JSONObject entry = new JSONObject();
		entry.put("id", "header_" + Math.abs(title.toJsonObject().toString().hashCode()));
		entry.put("kind", "header");
		entry.put("title", title.toJsonObject());
		entry.put("header_size", size);
		entry.put("header_center", center);
		return entry;
	}

	private static JSONObject entry(String id, String kind, LocalizedString title, LocalizedString body, JSONObject icon) {
		JSONObject entry = new JSONObject();
		entry.put("id", id);
		entry.put("kind", kind);
		entry.put("title", title.toJsonObject());
		entry.put("body", body.toJsonObject());
		entry.put("icon", icon);
		entry.put("second_icon", JSONObject.NULL);
		return entry;
	}

	private static void putSecondIcon(JSONObject entry, @Nullable JSONObject secondIcon) {
		entry.put("second_icon", secondIcon == null ? JSONObject.NULL : secondIcon);
	}

	private static JSONObject icon(String name) {
		JSONObject icon = new JSONObject();
		icon.put("type", "icon");
		icon.put("name", name);
		return icon;
	}

	private static JSONObject itemIcon(int image) {
		JSONObject icon = new JSONObject();
		icon.put("type", "item");
		icon.put("image", image);
		return icon;
	}

	private static JSONObject charIcon(String spriteName) {
		JSONObject icon = new JSONObject();
		icon.put("type", "char_sprite");
		icon.put("name", spriteName == null ? "" : spriteName);
		return icon;
	}

	private static JSONObject terrainIcon(int image) {
		JSONObject icon = new JSONObject();
		icon.put("type", "terrain_feature");
		icon.put("terrain_feature", image);
		return icon;
	}

	private static JSONObject badgeIcon(int image) {
		JSONObject icon = new JSONObject();
		icon.put("type", "badge");
		icon.put("image", image);
		return icon;
	}

	private static JSONObject textIcon(String text) {
		JSONObject icon = new JSONObject();
		icon.put("type", "text");
		icon.put("text", LocalizedString.raw(text).toJsonObject());
		return icon;
	}

	private static void applyAfterField(JSONArray array) {
		String prevId = null;
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.optJSONObject(i);
			if (obj != null && obj.has("id")) {
				if (prevId == null) {
					obj.put("after", JSONObject.NULL);
				} else {
					obj.put("after", prevId);
				}
				prevId = obj.getString("id");
			}
		}
	}

	private static LocalizedString msg(String owner, String key, Object... args) {
		return LocalizedString.key(new LocalizedKey(owner, key), args);
	}
}
