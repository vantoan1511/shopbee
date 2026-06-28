export function useLayout() {
    const colorMode = useColorMode();
    const sidebarVisible = useState<boolean>('sidebar-visible', () => true);

    const toggleDarkMode = () => {
        colorMode.preference = ['system', 'dark'].includes(colorMode.value) ? 'light' : 'dark';
    }

    const toggleSidebar = () => {
        sidebarVisible.value = !sidebarVisible.value;
    };

    const closeSidebar = () => {
        sidebarVisible.value = false;
    };

    const openSidebar = () => {
        sidebarVisible.value = true;
    };

    const isDarkMode = computed(() => colorMode.value === 'dark');

    return {
        isDarkMode,
        toggleDarkMode,
        sidebarVisible,
        toggleSidebar,
        closeSidebar,
        openSidebar
    };
}
