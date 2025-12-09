/*import React, { useState, useCallback } from "react";
import Cropper from "react-easy-crop";
import { getCroppedImg } from "../utils/cropImage"; // utility to convert crop to blob/dataURL

interface EditProfilePictureProps {
    onSave: (file: File) => void;
    currentPicture?: string | null;
}

export default function EditProfilePicture({ onSave, currentPicture }: EditProfilePictureProps) {
    const [imageSrc, setImageSrc] = useState<string | null>(null);
    const [crop, setCrop] = useState({ x: 0, y: 0 });
    const [zoom, setZoom] = useState(1);
    const [croppedAreaPixels, setCroppedAreaPixels] = useState<any>(null);

    const onCropComplete = useCallback((_, croppedAreaPixels) => {
        setCroppedAreaPixels(croppedAreaPixels);
    }, []);

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            const file = e.target.files[0];
            const reader = new FileReader();
            reader.addEventListener("load", () => {
                setImageSrc(reader.result as string);
            });
            reader.readAsDataURL(file);
        }
    };

    const handleSave = async () => {
        if (!imageSrc || !croppedAreaPixels) return;
        const croppedBlob = await getCroppedImg(imageSrc, croppedAreaPixels);
        const file = new File([croppedBlob], "profile.jpg", { type: "image/jpeg" });
        onSave(file);
        setImageSrc(null); // close editor
    };

    return (
        <div className="flex flex-col gap-2">
            {imageSrc ? (
                <div className="relative w-64 h-64 bg-gray-200 rounded-full overflow-hidden mx-auto">
                    <Cropper
                        image={imageSrc}
                        crop={crop}
                        zoom={zoom}
                        aspect={1}
                        cropShape="round"
                        showGrid={false}
                        onCropChange={setCrop}
                        onZoomChange={setZoom}
                        onCropComplete={onCropComplete}
                    />
                    <input
                        type="range"
                        min={1}
                        max={3}
                        step={0.1}
                        value={zoom}
                        onChange={(e) => setZoom(Number(e.target.value))}
                        className="mt-2"
                    />
                    <button
                        onClick={handleSave}
                        className="bg-blue-500 text-white px-4 py-2 rounded mt-2"
                    >
                        Save Photo
                    </button>
                </div>
            ) : (
                <>
                    <img
                        src={currentPicture || "/icon_SocialEX.png"}
                        alt="Current profile"
                        className="w-32 h-32 rounded-full border-2 border-gray-400 mx-auto"
                    />
                    <input type="file" accept="image/*" onChange={handleFileChange} />
                </>
            )}
        </div>
    );
}

 */
