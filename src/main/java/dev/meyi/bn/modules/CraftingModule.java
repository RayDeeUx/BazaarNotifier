package dev.meyi.bn.modules;

import com.google.gson.JsonIOException;
import dev.meyi.bn.BazaarNotifier;
import dev.meyi.bn.config.ModuleConfig;
import dev.meyi.bn.modules.calc.CraftingCalculator;
import dev.meyi.bn.utilities.ColorUtils;
import dev.meyi.bn.utilities.Defaults;
import dev.meyi.bn.utilities.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class CraftingModule extends Module {

  public static final ModuleName type = ModuleName.CRAFTING;
  private static boolean mouseButtonDown;
  private final LinkedHashMap<String, Color> helperLine = new LinkedHashMap<>();
  int longestXString;
  public static ArrayList<ArrayList<String>> list = new ArrayList<>();
  int lastHovered = 0;


  public CraftingModule() {
    super();
  }

  public CraftingModule(ModuleConfig module) {
    super(module);
  }

  private void generateHelperLine() {
    if (Mouse.isButtonDown(1) && getMouseCoordinateY() > y - 2 && getMouseCoordinateY() < y + 10) {
      int[] width = new int[4];
      int totalWidth = 0;
      int relativeX = getMouseCoordinateX() - x;
      width[0] =
          Minecraft.getMinecraft().fontRendererObj.getStringWidth(
                  BazaarNotifier.config.useBuyOrders? "   Profits (Buy Orders) -": "   Profits (Instant Buy) -");
      width[1] = BazaarNotifier.config.showInstantSellProfit ?
          Minecraft.getMinecraft().fontRendererObj.getStringWidth("  Instant Sell ") : 0;
      width[2] = BazaarNotifier.config.showSellOfferProfit ?
          Minecraft.getMinecraft().fontRendererObj.getStringWidth("/ Sell Offer ") : 0;
      width[3] = BazaarNotifier.config.showProfitPerMil ?
          Minecraft.getMinecraft().fontRendererObj.getStringWidth("/ 1m Instant") : 0;

      for (int i:width) {
        totalWidth += i;
      }

      for (int i = 3; i>= 0; i--) {
        totalWidth -= width[i];
        if(totalWidth < relativeX && inMovementBox()){
          switch (i){
            case 0: if(mouseButtonDown){
                        BazaarNotifier.config.useBuyOrders ^= true;
                        mouseButtonDown = false;
                    }
            case 1: BazaarNotifier.config.craftingSortingOption = 0;
                    break;
            case 2: BazaarNotifier.config.craftingSortingOption = 1;
                    break;
            case 3: BazaarNotifier.config.craftingSortingOption = 2;
                    break;
          }
          CraftingCalculator.getBestEnchantRecipes();
          break;
        }
      }
    }else if(!Mouse.isButtonDown(1)){
      mouseButtonDown = true;
    }
    helperLine.clear();
    helperLine.put("   ", Color.MAGENTA);
    helperLine.put(BazaarNotifier.config.useBuyOrders? "Profits (Buy Orders)": "Profits (Instant Buy)", Color.LIGHT_GRAY);
    if (BazaarNotifier.config.showProfitPerMil || BazaarNotifier.config.showInstantSellProfit
        || BazaarNotifier.config.showSellOfferProfit) {
      helperLine.put(" - ", Color.GRAY);
    }
    if (BazaarNotifier.config.showInstantSellProfit) {
      helperLine.put(" Instant Sell", BazaarNotifier.config.craftingSortingOption == 0 ?
              new Color(141, 152, 201): Color.LIGHT_GRAY);
      if (BazaarNotifier.config.showSellOfferProfit) {
        helperLine.put(" /", Color.GRAY);
      }
    }
    if (BazaarNotifier.config.showSellOfferProfit) {
      helperLine.put(" Sell Offer", BazaarNotifier.config.craftingSortingOption == 1 ?
              new Color(141, 152, 201): Color.LIGHT_GRAY);
      if (BazaarNotifier.config.showProfitPerMil) {
        helperLine.put(" / ", Color.GRAY);
      }
    }
    if (BazaarNotifier.config.showProfitPerMil) {
      helperLine.put("1m Instant", BazaarNotifier.config.craftingSortingOption == 2 ?
              new Color(141, 152, 201): Color.LIGHT_GRAY);
    }
  }

  @Override
  protected void draw() {
    if (BazaarNotifier.bazaarDataRaw != null) {
      List<LinkedHashMap<String, Color>> items = new ArrayList<>();
      generateHelperLine();
      items.add(helperLine);
      for (int i = shift; i < BazaarNotifier.config.craftingListLength + shift; i++) {
        LinkedHashMap<String, Color> message = new LinkedHashMap<>();
        if (i < list.size()) {
          if (!list.get(i).isEmpty()) {

            Double profitInstaSell = Double.valueOf(list.get(i).get(0));
            Double profitSellOffer = Double.valueOf(list.get(i).get(1));
            Double pricePerMil = Double.valueOf(list.get(i).get(2));
            String itemName = list.get(i).get(3);

            String itemNameConverted = BazaarNotifier.bazaarConv.get(itemName);
            message.put(String.valueOf(i + 1), Color.MAGENTA);
            message.put(". ", Color.MAGENTA);
            message.put(itemNameConverted, Color.CYAN);

            if (BazaarNotifier.config.showProfitPerMil
                || BazaarNotifier.config.showInstantSellProfit
                || BazaarNotifier.config.showSellOfferProfit) {
              message.put(" - ", Color.GRAY);
            }

            if (BazaarNotifier.config.showInstantSellProfit) {
              message.put(BazaarNotifier.df.format(profitInstaSell),
                  getColor(profitInstaSell.intValue()));
            }
            if (BazaarNotifier.config.showInstantSellProfit
                && BazaarNotifier.config.showSellOfferProfit) {
              message.put(" / ", Color.GRAY);
            }
            if (BazaarNotifier.config.showSellOfferProfit) {
              message.put(BazaarNotifier.df.format(profitSellOffer),
                  getColor(profitSellOffer.intValue()));
            }
            if (BazaarNotifier.config.showSellOfferProfit
                && BazaarNotifier.config.showProfitPerMil) {
              message.put(" /  ", Color.GRAY);
            }
            if (BazaarNotifier.config.showProfitPerMil) {
              message.put(BazaarNotifier.df.format(pricePerMil),
                  getColorForMil(pricePerMil.intValue()));
            }
          } else {
            message.put("Error, just wait", Color.RED);
          }
          items.add(message);
        }
      }
      this.longestXString = ColorUtils.drawColorfulParagraph(items, x, y, scale);
      boundsX = x + this.longestXString;
      renderMaterials(checkHoveredText(), list);
    } else {
      Utils.drawCenteredString("Waiting for bazaar data", x, y, 0xAAAAAA, scale);
      float X = x + 200 * scale;
      boundsX = (int) X;
    }
    float Y = y + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * scale
        * (BazaarNotifier.config.craftingListLength + 1)
        + (BazaarNotifier.config.craftingListLength + 1) * 2 * scale - 2;
    boundsY = (int) Y;
  }

  protected Color getColor(int price) {
    if (price <= 0) {
      return Color.RED;
    } else if (price <= 5000) {
      return Color.YELLOW;
    } else {
      return Color.GREEN;
    }
  }

  protected Color getColorForMil(int price) {
    if (price <= 0) {
      return Color.RED;
    } else if (price <= 30000) {
      return Color.YELLOW;
    } else {
      return Color.GREEN;
    }
  }

  @Override
  protected void reset() {
    x = Defaults.CRAFTING_MODULE_X;
    y = Defaults.CRAFTING_MODULE_Y;
    scale = 1;
    active = true;
    BazaarNotifier.config.craftingListLength = Defaults.CRAFTING_LIST_LENGTH;
  }

  @Override
  protected String name() {
    return ModuleName.CRAFTING.name();
  }

  @Override
  protected boolean shouldDrawBounds() {
    return true;
  }

  @Override
  protected int getMaxShift() {
    return list.size() - BazaarNotifier.config.craftingListLength;
  }

  protected int checkHoveredText() {
    float _y = y + 11 * scale;
    float y2 = _y + ((BazaarNotifier.config.craftingListLength) * 11 * scale);
    int mouseYFormatted = getMouseCoordinateY();
    int mouseXFormatted = getMouseCoordinateX();
    float relativeYMouse = (mouseYFormatted - _y) / (11 * scale);
    if (this.longestXString != 0) {
      if (mouseXFormatted >= x && mouseXFormatted <= x + longestXString
          && mouseYFormatted >= _y && mouseYFormatted <= y2 - 3 * scale) {
        return (int) relativeYMouse + shift;
      } else {
        return -1;
      }
    } else {
      return 1;
    }
  }

  protected void renderMaterials(int hoveredText, ArrayList<ArrayList<String>> list) {
    checkMouseMovement();
    List<LinkedHashMap<String, Color>> material = new ArrayList<>();
    LinkedHashMap<String, Color> text = new LinkedHashMap<>();

    if (hoveredText > -1) {
      if (hoveredText < list.size()) {
        if (BazaarNotifier.enchantCraftingList.getAsJsonObject("normal")
            .has(list.get(hoveredText).get(3))) {
          try {
            text.put(mouseWheelShift * 160 + "x ", Color.LIGHT_GRAY);
            text.put(BazaarNotifier.bazaarConv.get(
                BazaarNotifier.enchantCraftingList.getAsJsonObject("normal")
                    .getAsJsonObject(list.get(hoveredText).get(3)).get("material").getAsString()),
                Color.LIGHT_GRAY);
          } catch (JsonIOException e) {
            text.put("Error", Color.RED);
          }
        } else {
          int materialCount;
          StringBuilder _material = new StringBuilder();
          materialCount = BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
              .getAsJsonObject(list.get(hoveredText).get(3)).getAsJsonArray("material").size();
          for (int b = 0; b < materialCount / 2; b++) {
            if (b == 0) {
              _material.append((BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                  .getAsJsonObject(list.get(hoveredText).get(3)).getAsJsonArray("material").get(1)
                  .getAsInt()
                  * mouseWheelShift)).append("x ").append(BazaarNotifier.bazaarConv
                  .get(BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                      .getAsJsonObject(list.get(hoveredText).get(3)).getAsJsonArray("material")
                      .get(0).getAsString()));
            } else {
              _material.append(" | ").append(
                  BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                      .getAsJsonObject(list.get(hoveredText).get(3)).getAsJsonArray("material")
                      .get(b * 2 + 1).getAsInt() * mouseWheelShift).append("x ").append(
                  BazaarNotifier.bazaarConv.get(
                      BazaarNotifier.enchantCraftingList.getAsJsonObject("other")
                          .getAsJsonObject(list.get(hoveredText).get(3)).getAsJsonArray("material")
                          .get(b * 2).getAsString()));
            }
          }
          text.put(_material.toString(), Color.LIGHT_GRAY);
        }
        material.add(text);
        int longestXString = ColorUtils.drawColorfulParagraph(material, getMouseCoordinateX(),
            getMouseCoordinateY() - (int) (8 * scale), scale);
        Gui.drawRect(getMouseCoordinateX() - padding,
            getMouseCoordinateY() - (int) (8 * scale) - (int) (padding * scale),
            (int) (getMouseCoordinateX() + longestXString + padding * scale),
            (int) (getMouseCoordinateY() + padding * scale), 0xFF404040);
        ColorUtils.drawColorfulParagraph(material, getMouseCoordinateX(),
            getMouseCoordinateY() - (int) (8 * scale), scale);
      }
    }
  }


  public void checkMouseMovement() {
    if (lastHovered != checkHoveredText()) {
      mouseWheelShift = 1;
    }
    lastHovered = checkHoveredText();
  }
}