package org.ywzj.decoration.client.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.sighs.apricityui.init.Document;
import com.sighs.apricityui.init.Element;
import com.sighs.apricityui.render.Drawer;
import com.sighs.apricityui.screen.ApricityContainerMenu;
import com.sighs.apricityui.screen.ApricityScreen;
import com.sighs.apricityui.slot.ItemStackExpressionCompiler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.ywzj.decoration.all.AllRecipes;
import org.ywzj.decoration.client.resource.ClientAssetsManager;
import org.ywzj.decoration.client.resource.display.BaseDisplay;
import org.ywzj.decoration.client.resource.display.DecorationBedrockModel;
import org.ywzj.decoration.network.Channel;
import org.ywzj.decoration.network.message.ClientSewingAction;
import org.ywzj.decoration.recipe.SewingIngredient;
import org.ywzj.decoration.recipe.SewingRecipe;

import java.util.*;

public class SewingMachineScreen extends ApricityScreen implements MenuAccess<ApricityContainerMenu> {

    private static final String TEMPLATE = "screens/sewing_machine.html";
    private static final float PREVIEW_DEFAULT_PITCH = -165;
    private static final float PREVIEW_DEFAULT_YAW = -30;
    private static final double PREVIEW_FILL = 0.8D;
    private final ApricityContainerMenu menu;
    private final List<SewingRecipe> allRecipes = new ArrayList<>();
    private final List<RecipeEntry> recipeEntries = new ArrayList<>();
    private final List<MaterialView> materialViews = new ArrayList<>();
    private final List<ItemStack> inventorySnapshot = new ArrayList<>();
    private Document document;
    private Element recipeList;
    private Element recipeCount;
    private Element recipeEmpty;
    private Element selectedName;
    private Element previewAnchor;
    private Element materials;
    private Element materialsEmpty;
    private Element craftButton;
    private Element outputItem;
    private Element outputButton;
    private Element search;
    private SewingRecipe selected;
    private String filter = "";
    private String appliedQuery;
    private Boolean shownCraftReady;
    private ItemStack shownOutput = ItemStack.EMPTY;
    private boolean detailsDirty;
    private boolean hasMaterials;
    private float previewRotationX = PREVIEW_DEFAULT_PITCH;
    private float previewRotationY = PREVIEW_DEFAULT_YAW;
    private double previewZoom = 1.0D;
    private double previewPanX;
    private double previewPanY;
    private int previewDragButton = -1;

    public SewingMachineScreen(ApricityContainerMenu menu, Inventory inventory, Component title) {
        super(TEMPLATE);
        this.menu = menu;
        setPauseGame(false);
        setShowDefaultBackground(false);
    }

    @Override
    public ApricityContainerMenu getMenu() {
        return menu;
    }

    @Override
    protected void init() {
        appliedQuery = null;
        detailsDirty = true;
        shownCraftReady = null;
        shownOutput = ItemStack.EMPTY;
        inventorySnapshot.clear();
        super.init();
        document = getLinkedDocument();
        if (document == null) return;

        recipeList = element("recipe-list");
        recipeCount = element("recipe-count");
        recipeEmpty = element("recipe-empty");
        selectedName = element("selected-name");
        previewAnchor = element("decoration-preview");
        materials = element("materials");
        materialsEmpty = element("materials-empty");
        craftButton = element("craft-button");
        outputItem = element("output-item");
        outputButton = element("output-button");
        search = element("recipe-search");

        allRecipes.clear();
        if (minecraft != null && minecraft.level != null) {
            allRecipes.addAll(minecraft.level.getRecipeManager().getAllRecipesFor(AllRecipes.SEWING.get()));
        }
        allRecipes.sort(Comparator.comparing(this::recipeName).thenComparing(recipe -> recipe.getId().toString()));
        buildCatalog();
        if (search != null) {
            search.setValue(filter);
            search.setAttribute("placeholder", Component.translatable("screen.sewing_machine.search").getString());
        }
        Element close = element("close-button");
        if (close != null) close.addEventListener("click", event -> onClose());
        if (craftButton != null) craftButton.addEventListener("click", event -> craft());
        if (outputButton != null) outputButton.addEventListener("click", event -> takeOutput());
        applyFilter(filter);
        updateOutput(true);
        updateMachineState();
    }

    private Element element(String id) {
        return document.getElementById(id);
    }

    private String recipeName(SewingRecipe recipe) {
        return recipe.getResultItem(RegistryAccess.EMPTY).getHoverName().getString();
    }

    private void buildCatalog() {
        recipeEntries.clear();
        if (recipeList == null) return;
        recipeList.setTextContent("");
        for (SewingRecipe recipe : allRecipes) {
            Element entry = document.createElement("div");
            entry.setClassName("list-group-item recipe-entry");
            Element label = document.createElement("div");
            label.setClassName("recipe-entry-name");
            String name = recipeName(recipe);
            label.setTextContent(name);
            entry.appendChild(label);
            entry.addEventListener("click", event -> select(recipe));
            recipeList.appendChild(entry);
            String searchText = (name + " " + recipe.getId() + " " + recipe.getDisplayId()).toLowerCase(Locale.ROOT);
            recipeEntries.add(new RecipeEntry(recipe, entry, searchText));
        }
    }

    private void applyFilter(String raw) {
        filter = raw == null ? "" : raw;
        String query = filter.strip().toLowerCase(Locale.ROOT);
        if (query.equals(appliedQuery)) return;
        appliedQuery = query;

        int visible = 0;
        SewingRecipe first = null;
        SewingRecipe retained = null;
        for (RecipeEntry entry : recipeEntries) {
            boolean show = entry.searchText.contains(query);
            entry.element.setInlineStyleProperty("display", show ? "flex" : "none");
            document.markDirty(entry.element, Drawer.RELAYOUT | Drawer.REPAINT);
            if (show) {
                visible++;
                if (first == null) first = entry.recipe;
                if (selected != null && entry.recipe.getId().equals(selected.getId())) retained = entry.recipe;
            }
        }
        setText(recipeCount, visible + "/" + allRecipes.size());
        setHidden(recipeEmpty, visible > 0);
        select(retained != null ? retained : first);
    }

    private void select(SewingRecipe recipe) {
        boolean changed = selected != recipe;
        selected = recipe;
        for (RecipeEntry entry : recipeEntries) {
            String className = entry.recipe == recipe ? "list-group-item recipe-entry active" : "list-group-item recipe-entry";
            entry.element.setClassName(className);
            document.markDirty(entry.element, Drawer.RELAYOUT | Drawer.REPAINT);
        }
        if (changed || detailsDirty) {
            updateDetails();
            detailsDirty = false;
        }
        updateMachineState();
    }

    private void updateDetails() {
        materialViews.clear();
        if (materials != null) materials.setTextContent("");
        resetPreview();
        setHidden(materialsEmpty, selected != null);
        setText(selectedName, selected == null
                ? Component.translatable("screen.sewing_machine.select_recipe").getString()
                : recipeName(selected));
        if (selected != null) {
            if (materials != null) {
                for (SewingIngredient input : selected.getMaterials()) createMaterialRow(input);
            }
        }
        updateMaterialCounts();
    }

    private void resetPreview() {
        previewRotationX = PREVIEW_DEFAULT_PITCH;
        previewRotationY = PREVIEW_DEFAULT_YAW;
        previewZoom = 1.0D;
        previewPanX = 0.0D;
        previewPanY = 0.0D;
        previewDragButton = -1;
        if (previewAnchor != null) previewAnchor.setClassName("");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderPreview(graphics);
    }

    private void renderPreview(GuiGraphics graphics) {
        if (selected == null) return;
        PreviewBounds bounds = getPreviewBounds();
        if (bounds == null) return;
        BaseDisplay display = ClientAssetsManager.INSTANCE.getDecorationDisplay(selected.getDisplayId()).orElse(null);
        if (display == null) return;
        DecorationBedrockModel model = display.getModel();
        ResourceLocation texture = display.getTexture();
        if (model == null || texture == null || !model.hasBakedModel()) return;
        AABB box = model.getGeometryBoundingBox();
        if (box == null) return;
        Vec3 center = box.getCenter();
        double pitch = Math.toRadians(PREVIEW_DEFAULT_PITCH);
        double yaw = Math.toRadians(PREVIEW_DEFAULT_YAW);
        double projectedWidth = Math.max(0.01D,
                Math.abs(Math.cos(yaw)) * box.getXsize() + Math.abs(Math.sin(yaw)) * box.getZsize());
        double rotatedDepth = Math.abs(Math.sin(yaw)) * box.getXsize() + Math.abs(Math.cos(yaw)) * box.getZsize();
        double projectedHeight = Math.max(0.01D,
                Math.abs(Math.cos(pitch)) * box.getYsize() + Math.abs(Math.sin(pitch)) * rotatedDepth);
        float scale = (float) (PREVIEW_FILL * Math.min(bounds.width() / projectedWidth,
                bounds.height() / projectedHeight) * previewZoom);
        graphics.enableScissor((int) Math.floor(bounds.left), (int) Math.floor(bounds.top),
                (int) Math.ceil(bounds.right), (int) Math.ceil(bounds.bottom));
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        try {
            poseStack.translate((bounds.left + bounds.right) / 2 + previewPanX * bounds.width(),
                    (bounds.top + bounds.bottom) / 2 + previewPanY * bounds.height(), 512.0D);
            poseStack.mulPoseMatrix(new Matrix4f().scaling(scale, scale, -scale));
            poseStack.mulPose(Axis.XP.rotationDegrees(previewRotationX));
            poseStack.mulPose(Axis.YP.rotationDegrees(previewRotationY));
            poseStack.translate(-center.x, -center.y, -center.z);
            Lighting.setupForEntityInInventory();
            RenderSystem.runAsFancy(() -> model.renderToBuffer(poseStack, graphics.bufferSource(), texture, 15728880));
            graphics.flush();
        } finally {
            Lighting.setupFor3DItems();
            poseStack.popPose();
            graphics.disableScissor();
        }
    }

    private PreviewBounds getPreviewBounds() {
        if (previewAnchor == null || document == null) return null;
        Element.DOMRect rect = previewAnchor.getBoundingClientRect();
        if (rect.width <= 0 || rect.height <= 0) return null;
        double scale = document.getViewport().renderScale();
        return new PreviewBounds(rect.x * scale, rect.y * scale,
                (rect.x + rect.width) * scale, (rect.y + rect.height) * scale);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        PreviewBounds bounds = getPreviewBounds();
        if (selected != null && (button == 0 || button == 1)
                && bounds != null && bounds.contains(mouseX, mouseY)) {
            previewDragButton = button;
            previewAnchor.setClassName("dragging");
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == previewDragButton) {
            if (button == 0) {
                previewRotationY = Mth.wrapDegrees(previewRotationY + (float) dragX);
                previewRotationX = Mth.wrapDegrees(previewRotationX + (float) dragY);
                return true;
            }
            PreviewBounds bounds = getPreviewBounds();
            if (button == 1 && bounds != null) {
                previewPanX = Mth.clamp(previewPanX + dragX / bounds.width(), -0.5D, 0.5D);
                previewPanY = Mth.clamp(previewPanY + dragY / bounds.height(), -0.5D, 0.5D);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == previewDragButton) {
            previewDragButton = -1;
            if (previewAnchor != null) previewAnchor.setClassName("");
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        PreviewBounds bounds = getPreviewBounds();
        if (!hasControlDown() && selected != null && bounds != null && bounds.contains(mouseX, mouseY)) {
            previewZoom = Mth.clamp(previewZoom * Math.pow(1.1D, delta), 0.25D, 4.0D);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private record PreviewBounds(double left, double top, double right, double bottom) {
        private double width() { return right - left; }
        private double height() { return bottom - top; }
        private boolean contains(double x, double y) {
            return x >= left && x < right && y >= top && y < bottom;
        }
    }

    private void createMaterialRow(SewingIngredient input) {
        ItemStack[] choices = input.ingredient().getItems();
        Element row = document.createElement("div");
        row.setClassName("material-row");
        Element slot = document.createElement("slot");
        slot.setClassName("requirement-slot");
        slot.setAttribute("size", "36");
        Element ingredient = document.createElement("ingredient");
        ingredient.setAttribute("cycle-interval", "900");
        ingredient.setTextContent(joinItemIds(choices));
        slot.appendChild(ingredient);
        Element copy = document.createElement("div");
        copy.setClassName("material-copy");
        Element name = document.createElement("div");
        name.setClassName("material-name");
        name.setTextContent(choices.length == 0
                ? Component.translatable("screen.sewing_machine.unavailable_material").getString()
                : choices[0].getHoverName().getString());
        copy.appendChild(name);
        Element count = document.createElement("div");
        int owned = countOwned(input);
        updateCount(count, input, owned);
        row.appendChild(slot);
        row.appendChild(copy);
        row.appendChild(count);
        materials.appendChild(row);
        materialViews.add(new MaterialView(input, count, owned));
    }

    private String joinItemIds(ItemStack[] choices) {
        Set<ResourceLocation> ids = new LinkedHashSet<>();
        for (ItemStack choice : choices) ids.add(BuiltInRegistries.ITEM.getKey(choice.getItem()));
        return ids.stream().map(ResourceLocation::toString).reduce((left, right) -> left + "|" + right).orElse("");
    }

    private int countOwned(SewingIngredient input) {
        int owned = 0;
        for (ItemStack stack : menu.getPlayerInventory().items) {
            if (!stack.isEmpty() && input.ingredient().test(stack)) owned += stack.getCount();
        }
        return owned;
    }

    private void updateCount(Element count, SewingIngredient input, int owned) {
        count.setClassName(owned < input.count() ? "material-count missing" : "material-count");
        count.setTextContent(owned + " / " + input.count());
    }

    private boolean inventoryChanged() {
        List<ItemStack> stacks = menu.getPlayerInventory().items;
        boolean changed = inventorySnapshot.size() != stacks.size();
        for (int i = 0; !changed && i < stacks.size(); i++) {
            changed = !ItemStack.matches(inventorySnapshot.get(i), stacks.get(i));
        }
        if (changed) {
            inventorySnapshot.clear();
            for (ItemStack stack : stacks) inventorySnapshot.add(stack.copy());
        }
        return changed;
    }

    private void updateMaterialCounts() {
        for (MaterialView view : materialViews) {
            int owned = countOwned(view.input);
            if (owned != view.shownOwned) {
                updateCount(view.count, view.input, owned);
                document.markDirty(view.count, Drawer.RELAYOUT | Drawer.REPAINT);
                view.shownOwned = owned;
            }
        }
        hasMaterials = selected != null && selected.planConsumption(menu.getPlayerInventory()) != null;
    }

    private void updateOutput(boolean force) {
        ItemStack stack = menu.getSlot(0).getItem();
        if (!force && ItemStack.matches(shownOutput, stack)) return;
        shownOutput = stack.copy();
        setText(outputItem, stack.isEmpty() ? "minecraft:air" : ItemStackExpressionCompiler.serialize(stack));
        if (outputButton != null) outputButton.setDisabled(stack.isEmpty());
    }

    private void updateMachineState() {
        boolean ready = selected != null && hasMaterials && !menu.getSlot(0).hasItem();
        if (shownCraftReady != null && shownCraftReady == ready) return;
        shownCraftReady = ready;
        if (craftButton != null) craftButton.setDisabled(!ready);
    }

    private void craft() {
        updateMaterialCounts();
        updateMachineState();
        if (Boolean.TRUE.equals(shownCraftReady)) {
            Channel.CHANNEL.sendToServer(new ClientSewingAction(menu.containerId, selected.getId()));
        }
    }

    private void takeOutput() {
        if (minecraft == null || minecraft.player == null || minecraft.gameMode == null
                || minecraft.player.containerMenu != menu || !menu.getSlot(0).hasItem()) return;
        minecraft.gameMode.handleInventoryMouseClick(menu.containerId, 0, 0, ClickType.QUICK_MOVE, minecraft.player);
        updateOutput(false);
        updateMachineState();
    }

    private static void setText(Element element, String text) {
        if (element != null) element.setTextContent(text);
    }

    private static void setHidden(Element element, boolean hidden) {
        if (element != null) element.setClassName(hidden ? "empty-state hidden" : "empty-state");
    }

    @Override
    public void tick() {
        super.tick();
        if (minecraft == null || minecraft.player == null || !minecraft.player.isAlive()
                || minecraft.player.isRemoved() || minecraft.player.containerMenu != menu) {
            onClose();
            return;
        }
        if (document == null) return;
        if (search != null) {
            String value = search.getValue() == null ? "" : search.getValue();
            if (!filter.equals(value)) applyFilter(value);
        }
        if (inventoryChanged()) updateMaterialCounts();
        updateOutput(false);
        updateMachineState();
    }

    @Override
    public void onClose() {
        if (minecraft != null && minecraft.player != null && minecraft.player.containerMenu == menu) {
            minecraft.player.closeContainer();
        }
        super.onClose();
    }

    private record RecipeEntry(SewingRecipe recipe, Element element, String searchText) {}

    private static final class MaterialView {
        private final SewingIngredient input;
        private final Element count;
        private int shownOwned;

        private MaterialView(SewingIngredient input, Element count, int shownOwned) {
            this.input = input;
            this.count = count;
            this.shownOwned = shownOwned;
        }
    }
}
