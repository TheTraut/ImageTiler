#!/usr/bin/env python3
"""
Generate a calibration rectangle image for printer calibration.
Creates a simple white background with a black outline rectangle of known dimensions.
"""

from PIL import Image, ImageDraw, ImageFont
import os

def create_calibration_image():
    # Image dimensions (3300 x 2550 pixels as expected by the app)
    width = 3300
    height = 2550
    
    # Create white background
    image = Image.new('RGB', (width, height), 'white')
    draw = ImageDraw.Draw(image)
    
    # Calculate rectangle dimensions
    # Rectangle should be 4.0 x 3.0 inches at 150 DPI
    # 4.0 inches * 150 DPI = 600 pixels wide
    # 3.0 inches * 150 DPI = 450 pixels tall
    rect_width = 600
    rect_height = 450
    
    # Center the rectangle
    rect_x = (width - rect_width) // 2
    rect_y = (height - rect_height) // 2
    
    # Draw the black outline rectangle (no fill)
    line_width = 3  # Make the outline thick enough to be easily visible
    draw.rectangle([rect_x, rect_y, rect_x + rect_width, rect_y + rect_height], 
                   fill=None, outline='black', width=line_width)
    
    # Add some text labels for clarity
    try:
        # Try to use a default font, fall back to default if not available
        font_size = 40
        try:
            font = ImageFont.truetype("/System/Library/Fonts/Arial.ttf", font_size)
        except:
            try:
                font = ImageFont.truetype("/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf", font_size)
            except:
                font = ImageFont.load_default()
        
        # Add title
        title_text = "PRINTER CALIBRATION SHEET"
        title_bbox = draw.textbbox((0, 0), title_text, font=font)
        title_width = title_bbox[2] - title_bbox[0]
        title_x = (width - title_width) // 2
        draw.text((title_x, 100), title_text, fill='black', font=font)
        
        # Add instructions
        instruction_text = "Measure this rectangle outline: 4.0 x 3.0 inches"
        instruction_bbox = draw.textbbox((0, 0), instruction_text, font=font)
        instruction_width = instruction_bbox[2] - instruction_bbox[0]
        instruction_x = (width - instruction_width) // 2
        draw.text((instruction_x, rect_y + rect_height + 50), instruction_text, fill='black', font=font)
        
        # Add dimension labels
        small_font_size = 30
        try:
            small_font = ImageFont.truetype("/System/Library/Fonts/Arial.ttf", small_font_size)
        except:
            small_font = font
            
        # Width label (top of rectangle)
        width_text = "4.0 inches"
        width_bbox = draw.textbbox((0, 0), width_text, font=small_font)
        width_text_width = width_bbox[2] - width_bbox[0]
        width_text_x = rect_x + (rect_width - width_text_width) // 2
        draw.text((width_text_x, rect_y - 40), width_text, fill='black', font=small_font)
        
        # Height label (left side of rectangle)
        height_text = "3.0 inches"
        draw.text((rect_x - 120, rect_y + rect_height // 2), height_text, fill='black', font=small_font)
        
        # Add measurement tips
        tip_font_size = 24
        try:
            tip_font = ImageFont.truetype("/System/Library/Fonts/Arial.ttf", tip_font_size)
        except:
            tip_font = small_font
            
        tip_text = "Measure from outer edge to outer edge of the rectangle outline"
        tip_bbox = draw.textbbox((0, 0), tip_text, font=tip_font)
        tip_width = tip_bbox[2] - tip_bbox[0]
        tip_x = (width - tip_width) // 2
        draw.text((tip_x, rect_y + rect_height + 100), tip_text, fill='black', font=tip_font)
        
    except Exception as e:
        print(f"Warning: Could not add text labels: {e}")
    
    return image

def main():
    # Create the calibration image
    image = create_calibration_image()
    
    # Save to the resources directory
    output_path = "src/main/resources/calibration/calibration.png"
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    image.save(output_path, 'PNG')
    
    print(f"Calibration image saved to: {output_path}")
    print(f"Image dimensions: {image.width} x {image.height} pixels")
    print("Rectangle dimensions: 600 x 450 pixels (4.0 x 3.0 inches at 150 DPI)")
    print("Rectangle style: Black outline (no fill) - saves ink and easier to measure!")

if __name__ == "__main__":
    main()
