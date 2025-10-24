#include <stdio.h>
#include <sys/io.h>
#include <string.h>
#include "pci_c_header.h"

#define PCI_CONFIG_ADDRESS 0xCF8
#define PCI_CONFIG_DATA    0xCFC

extern PCI_VENTABLE PciVenTable[];
typedef unsigned char u8;
typedef unsigned int u32;
typedef unsigned short u16;

u32 pci_config_address(u8 bus, u8 device, u8 func, u8 offset) {
    return (u32)(
        (1 << 31) |
        (bus << 16) |
        (device << 11) |
        (func << 8) |
        (offset & 0xFC)
    );
}

u32 pci_read_config_dword(u8 bus, u8 device, u8 func, u8 offset) {
    u32 address = pci_config_address(bus, device, func, offset);
    outl(address, PCI_CONFIG_ADDRESS);
    return inl(PCI_CONFIG_DATA);
}

u16 pci_read_config_word(u8 bus, u8 device, u8 func, u8 offset) {
    u32 data = pci_read_config_dword(bus, device, func, offset & 0xFC);
    return (u16)((data >> ((offset & 2) * 8)) & 0xFFFF);
}

u8 pci_read_config_byte(u8 bus, u8 device, u8 func, u8 offset) {
    u32 data = pci_read_config_dword(bus, device, func, offset & 0xFC);
    return (u8)((data >> ((offset & 3) * 8)) & 0xFF);
}

const char* get_vendor_name(u16 vendor_id) {
    for (u32 i = 0; i < PCI_VENTABLE_LEN; i++) {
        if (PciVenTable[i].VenId == vendor_id) {
            if (PciVenTable[i].VenFull != NULL && strlen(PciVenTable[i].VenFull) > 0) {
                return PciVenTable[i].VenFull;
            }
            return PciVenTable[i].VenShort;
        }
    }
    return "Unknown Vendor";
}

const char* decode_interrupt_pin(u8 pin) {
    switch(pin) {
        case 0: return "No interrupt";
        case 1: return "INTA#";
        case 2: return "INTB#";
        case 3: return "INTC#";
        case 4: return "INTD#";
        default: return "Reserved";
    }
}

void print_device_header(int device_count, u8 bus, u8 device, u8 func,
                         u16 vendor_id, u16 device_id) {
    printf("-----------------------------------------------------------------\n");
    printf("Device #%d\n", device_count);
    printf("   Address: Bus=%02X, Device=%02X, Function=%02X\n", bus, device, func);
    printf("   Vendor ID: 0x%04X (%s)\n", vendor_id, get_vendor_name(vendor_id));
    printf("   Device ID: 0x%04X\n", device_id);
}

void print_interrupt_info(u8 bus, u8 device, u8 func) {
    u8 int_pin = pci_read_config_byte(bus, device, func, 0x3D);
    printf("   Interrupt Pin: 0x%02X (%s)\n", int_pin, decode_interrupt_pin(int_pin));

    u8 int_line = pci_read_config_byte(bus, device, func, 0x3C);
    printf("   Interrupt Line: 0x%02X", int_line);

    if (int_line == 0xFF)
        printf(" (No connection or not used)\n");
    else
        printf(" (IRQ %d)\n", int_line);
}

void print_bridge_info(u8 bus, u8 device, u8 func) {
    u8 primary_bus = pci_read_config_byte(bus, device, func, 0x18);
    u8 secondary_bus = pci_read_config_byte(bus, device, func, 0x19);
    u8 subordinate_bus = pci_read_config_byte(bus, device, func, 0x1A);

    printf("\n   === Bridge Information ===\n");
    printf("   Primary Bus Number: 0x%02X (Bus %d)\n", primary_bus, primary_bus);
    printf("   Secondary Bus Number: 0x%02X (Bus %d)\n", secondary_bus, secondary_bus);
    printf("   Subordinate Bus Number: 0x%02X (Bus %d)\n", subordinate_bus, subordinate_bus);
}

int is_valid_device(u16 vendor_id) {
    return (vendor_id != 0xFFFF && vendor_id != 0x0000);
}

u8 get_max_functions(u8 bus, u8 device) {
    u8 header_type = pci_read_config_byte(bus, device, 0, 0x0E);
    return (header_type & 0x80) ? 8 : 1;
}

void process_device_function(u8 bus, u8 device, u8 func, int device_count) {
    u16 vendor_id = pci_read_config_word(bus, device, func, 0x00);
    u16 device_id = pci_read_config_word(bus, device, func, 0x02);

    print_device_header(device_count, bus, device, func, vendor_id, device_id);

    u8 header_type = pci_read_config_byte(bus, device, func, 0x0E);
    u8 header_type_base = header_type & 0x7F;

    if (header_type_base == 0x00)
        print_interrupt_info(bus, device, func);

    if (header_type_base == 0x01)
        print_bridge_info(bus, device, func);

    printf("\n");
}

int scan_device(u8 bus, u8 device, int device_count) {
    u16 vendor_id = pci_read_config_word(bus, device, 0, 0x00);

    if (!is_valid_device(vendor_id))
        return device_count;

    u8 max_func = get_max_functions(bus, device);

    for (u8 func = 0; func < max_func; func++) {
        vendor_id = pci_read_config_word(bus, device, func, 0x00);

        if (!is_valid_device(vendor_id))
            continue;

        device_count++;
        process_device_function(bus, device, func, device_count);
    }

    return device_count;
}

int main() {
    printf("=================================================================\n");
    printf("           PCI Device Scanner - Variant 7\n");
    printf("=================================================================\n\n");

    if (iopl(3)) {
        printf("ERROR: Cannot get I/O privileges\n");
        printf("Please run as root: sudo ./program\n");
        return 1;
    }

    printf("Scanning PCI buses...\n\n");

    int device_count = 0;

    for (u8 bus = 0; bus < 8; bus++)
        for (u8 device = 0; device < 32; device++)
            device_count = scan_device(bus, device, device_count);

    printf("=================================================================\n");
    printf("Total devices found: %d\n", device_count);
    printf("=================================================================\n");

    return 0;
}
