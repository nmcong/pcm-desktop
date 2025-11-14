#!/bin/bash

# ================================================================
# Lucide Icon Downloader
# Downloads PNG icons from lucide.dev
# ================================================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Script directory and project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
ICONS_DIR="${PROJECT_ROOT}/src/main/resources/images/icons"

# Create icons directory if it doesn't exist
mkdir -p "$ICONS_DIR"

# Function to display help
show_help() {
    echo -e "${BLUE}Lucide Icon Downloader${NC}"
    echo ""
    echo "Usage: $0 [OPTIONS] ICON_NAME"
    echo ""
    echo "Downloads PNG icons from https://lucide.dev/icons/"
    echo ""
    echo "Options:"
    echo "  -s, --size SIZE     Icon size (default: 24)"
    echo "                      Available: 16, 20, 24, 32, 48, 64, 96, 128"
    echo "  -c, --color COLOR   Icon color (default: 000000)"
    echo "                      Format: hex color without # (e.g., ff0000 for red)"
    echo "  -o, --output DIR    Output directory (default: src/main/resources/images/icons)"
    echo "  -l, --list          List popular icon names"
    echo "  -h, --help          Show this help"
    echo ""
    echo "Examples:"
    echo "  $0 home                    # Download home icon (24px, black)"
    echo "  $0 -s 32 user              # Download user icon (32px, black)"
    echo "  $0 -s 48 -c ff0000 heart   # Download red heart icon (48px)"
    echo "  $0 --size 64 --color 0969da github"
    echo ""
    echo "Popular icons:"
    echo "  home, user, settings, search, menu, close, check, plus, minus"
    echo "  heart, star, bookmark, file, folder, image, video, download"
    echo "  edit, delete, save, copy, share, arrow-left, arrow-right"
    echo "  mail, phone, calendar, clock, bell, lock, unlock, eye"
}

# Function to list popular icons
list_icons() {
    echo -e "${BLUE}Popular Lucide Icons:${NC}"
    echo ""
    
    categories=(
        "Navigation:home,menu,arrow-left,arrow-right,arrow-up,arrow-down,chevron-left,chevron-right,chevron-up,chevron-down"
        "Actions:plus,minus,edit,delete,save,copy,share,download,upload,refresh,undo,redo"
        "Interface:search,settings,user,bell,mail,phone,calendar,clock,eye,eye-off,lock,unlock"
        "Content:file,folder,image,video,music,book,bookmark,heart,star,flag,tag,paperclip"
        "Status:check,x,alert-circle,alert-triangle,info,help-circle,thumbs-up,thumbs-down"
        "Social:github,twitter,facebook,instagram,linkedin,youtube,discord,slack"
        "Media:play,pause,stop,skip-back,skip-forward,volume,volume-off,camera,mic,mic-off"
    )
    
    for category in "${categories[@]}"; do
        IFS=':' read -ra PARTS <<< "$category"
        category_name="${PARTS[0]}"
        icons="${PARTS[1]}"
        
        echo -e "${YELLOW}${category_name}:${NC}"
        echo "  ${icons}" | tr ',' ' '
        echo ""
    done
    
    echo -e "${BLUE}Visit https://lucide.dev/icons for the complete list${NC}"
}

# Function to validate icon name
validate_icon_name() {
    local icon_name="$1"
    
    # Check if icon name is valid (alphanumeric, hyphens, underscores)
    if [[ ! "$icon_name" =~ ^[a-zA-Z0-9_-]+$ ]]; then
        echo -e "${RED}Error: Invalid icon name. Use only letters, numbers, hyphens, and underscores.${NC}"
        return 1
    fi
    
    return 0
}

# Function to validate size
validate_size() {
    local size="$1"
    local valid_sizes=(16 20 24 32 48 64 96 128)
    
    for valid_size in "${valid_sizes[@]}"; do
        if [[ "$size" == "$valid_size" ]]; then
            return 0
        fi
    done
    
    echo -e "${RED}Error: Invalid size. Valid sizes: ${valid_sizes[*]}${NC}"
    return 1
}

# Function to validate color
validate_color() {
    local color="$1"
    
    # Check if color is valid hex (6 characters, alphanumeric)
    if [[ ! "$color" =~ ^[0-9a-fA-F]{6}$ ]]; then
        echo -e "${RED}Error: Invalid color. Use 6-digit hex color without # (e.g., ff0000)${NC}"
        return 1
    fi
    
    return 0
}

# Function to download icon
download_icon() {
    local icon_name="$1"
    local size="$2"
    local color="$3"
    local output_dir="$4"
    
    # Construct URL
    local url="https://api.iconify.design/lucide:${icon_name}.svg?color=%23${color}&width=${size}&height=${size}"
    local filename="${icon_name}-${size}px-${color}.svg"
    local output_path="${output_dir}/${filename}"
    
    echo -e "${BLUE}Downloading ${icon_name} icon...${NC}"
    echo "  Size: ${size}px"
    echo "  Color: #${color}"
    echo "  URL: ${url}"
    echo "  Output: ${output_path}"
    echo ""
    
    # Download with curl
    if command -v curl &> /dev/null; then
        if curl -L -o "$output_path" "$url" 2>/dev/null; then
            if [[ -f "$output_path" && -s "$output_path" ]]; then
                echo -e "${GREEN}✓ Successfully downloaded: ${filename}${NC}"
                
                # Also try to download as PNG if possible
                local png_url="https://api.iconify.design/lucide:${icon_name}.png?color=%23${color}&width=${size}&height=${size}"
                local png_filename="${icon_name}-${size}px-${color}.png"
                local png_output_path="${output_dir}/${png_filename}"
                
                echo -e "${BLUE}Attempting to download PNG version...${NC}"
                if curl -L -o "$png_output_path" "$png_url" 2>/dev/null; then
                    if [[ -f "$png_output_path" && -s "$png_output_path" ]]; then
                        echo -e "${GREEN}✓ Successfully downloaded PNG: ${png_filename}${NC}"
                    else
                        rm -f "$png_output_path" 2>/dev/null
                        echo -e "${YELLOW}⚠ PNG version not available, SVG downloaded instead${NC}"
                    fi
                else
                    echo -e "${YELLOW}⚠ PNG version not available, SVG downloaded instead${NC}"
                fi
                
                return 0
            else
                rm -f "$output_path" 2>/dev/null
                echo -e "${RED}✗ Downloaded file is empty or corrupted${NC}"
                return 1
            fi
        else
            echo -e "${RED}✗ Failed to download icon${NC}"
            return 1
        fi
    else
        echo -e "${RED}Error: curl is not installed. Please install curl to use this script.${NC}"
        return 1
    fi
}

# Function to check if icon exists
check_icon_exists() {
    local icon_name="$1"
    local check_url="https://api.iconify.design/lucide:${icon_name}.svg"
    
    echo -e "${BLUE}Checking if icon '${icon_name}' exists...${NC}"
    
    if command -v curl &> /dev/null; then
        local response_code=$(curl -s -o /dev/null -w "%{http_code}" "$check_url")
        if [[ "$response_code" == "200" ]]; then
            echo -e "${GREEN}✓ Icon '${icon_name}' exists${NC}"
            return 0
        else
            echo -e "${RED}✗ Icon '${icon_name}' not found${NC}"
            echo -e "${YELLOW}Try one of these similar icons:${NC}"
            
            # Suggest similar icons based on common patterns
            case "$icon_name" in
                *home*) echo "  home, house, building, home-2" ;;
                *user*) echo "  user, user-2, users, account, profile" ;;
                *search*) echo "  search, find, zoom-in, magnifying-glass" ;;
                *menu*) echo "  menu, hamburger, more-horizontal, more-vertical" ;;
                *settings*) echo "  settings, cog, gear, preferences, config" ;;
                *edit*) echo "  edit, pencil, pen, write, modify" ;;
                *delete*) echo "  delete, trash, trash-2, remove, x" ;;
                *) echo "  Visit https://lucide.dev/icons to browse all icons" ;;
            esac
            return 1
        fi
    else
        echo -e "${YELLOW}⚠ Cannot check icon existence (curl not available)${NC}"
        return 0
    fi
}

# Default values
size="24"
color="000000"
output_dir="$ICONS_DIR"
icon_name=""

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -s|--size)
            size="$2"
            shift 2
            ;;
        -c|--color)
            color="$2"
            shift 2
            ;;
        -o|--output)
            output_dir="$2"
            shift 2
            ;;
        -l|--list)
            list_icons
            exit 0
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        -*|--*)
            echo -e "${RED}Error: Unknown option $1${NC}"
            echo "Use -h or --help for usage information."
            exit 1
            ;;
        *)
            if [[ -z "$icon_name" ]]; then
                icon_name="$1"
            else
                echo -e "${RED}Error: Multiple icon names specified. Please specify only one icon name.${NC}"
                exit 1
            fi
            shift
            ;;
    esac
done

# Check if icon name is provided
if [[ -z "$icon_name" ]]; then
    echo -e "${RED}Error: Please specify an icon name.${NC}"
    echo ""
    show_help
    exit 1
fi

# Validate inputs
if ! validate_icon_name "$icon_name"; then
    exit 1
fi

if ! validate_size "$size"; then
    exit 1
fi

if ! validate_color "$color"; then
    exit 1
fi

# Create output directory
mkdir -p "$output_dir"
if [[ ! -d "$output_dir" ]]; then
    echo -e "${RED}Error: Cannot create output directory: $output_dir${NC}"
    exit 1
fi

# Check if icon exists before downloading
if ! check_icon_exists "$icon_name"; then
    echo ""
    echo -e "${BLUE}You can browse all available icons at: https://lucide.dev/icons${NC}"
    exit 1
fi

echo ""

# Download the icon
if download_icon "$icon_name" "$size" "$color" "$output_dir"; then
    echo ""
    echo -e "${GREEN}🎉 Icon downloaded successfully!${NC}"
    echo ""
    echo "Files created in: $output_dir"
    echo "  • ${icon_name}-${size}px-${color}.svg"
    if [[ -f "${output_dir}/${icon_name}-${size}px-${color}.png" ]]; then
        echo "  • ${icon_name}-${size}px-${color}.png"
    fi
    
    # Copy to build directory
    echo ""
    echo -e "${BLUE}📋 Copying to build directory...${NC}"
    BUILD_DIR="${PROJECT_ROOT}/out/images/icons"
    mkdir -p "$BUILD_DIR"
    
    # Copy the downloaded files
    cp "${output_dir}/${icon_name}-${size}px-${color}.svg" "$BUILD_DIR/" 2>/dev/null
    if [[ -f "${output_dir}/${icon_name}-${size}px-${color}.png" ]]; then
        cp "${output_dir}/${icon_name}-${size}px-${color}.png" "$BUILD_DIR/" 2>/dev/null
        echo -e "${GREEN}✓ Copied PNG to build directory${NC}"
    fi
    echo -e "${GREEN}✓ Copied SVG to build directory${NC}"
    
    echo ""
    echo -e "${BLUE}💡 Usage in JavaFX:${NC}"
    echo "  // For SVG (using SVGPath or similar)"
    echo "  String iconPath = \"/images/icons/${icon_name}-${size}px-${color}.svg\";"
    echo ""
    echo "  // For PNG (using ImageView)"
    echo "  Image icon = new Image(\"/images/icons/${icon_name}-${size}px-${color}.png\");"
    echo "  ImageView iconView = new ImageView(icon);"
else
    echo ""
    echo -e "${RED}💥 Failed to download icon${NC}"
    exit 1
fi